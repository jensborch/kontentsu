/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.cdn.externalization;

import dk.kontentsu.cdn.externalization.visitors.ExternalizationVisitor;
import dk.kontentsu.cdn.externalization.visitors.ExternalizationVisitorSupplier;
import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.model.internal.ReferenceProcessor;
import dk.kontentsu.cdn.model.internal.ReferenceType;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTree;
import dk.kontentsu.cdn.model.internal.Version;
import dk.kontentsu.cdn.repository.ExternalFileRepository;
import dk.kontentsu.cdn.repository.ItemRepository;
import dk.kontentsu.cdn.spi.ContentContext;
import dk.kontentsu.cdn.spi.ContentProcessingMimeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service facade for externalizing internal content.
 *
 * @author Jens Borch Christiansen
 */
@LocalBean
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExternalizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalizerService.class);

    private final Set<UUID> processing = new HashSet<>();

    @Inject
    private ExternalFileRepository fileRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private ScheduledExternalizerService scheduleService;

    @Inject
    private ExternalizationVisitorSupplier visitorSupplier;

    @Inject
    private BeanManager bm;

    private boolean processing(final UUID uuid) {
        synchronized (processing) {
            if (processing.contains(uuid)) {
                return true;
            } else {
                processing.add(uuid);
                return false;
            }
        }
    }

    private void processed(final UUID uuid) {
        synchronized (processing) {
            processing.remove(uuid);
        }
    }

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Future<List<ExternalFile>> externalize(final UUID version) {
        Version v = itemRepo.getVersion(version);
        LOGGER.info("Externalizing version {} and its references", version);
        return new AsyncResult<>(externalize(v));

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<ExternalFile> externalize(final Version version) {
        List<Version> versions = findVersionsToExternalize(version);
        return versions.stream()
                .flatMap(v -> externalizeVersion(v).stream())
                .sorted((f1, f2) -> f1.getInterval().getFrom().compareTo(f2.getInterval().getFrom()))
                .collect(Collectors.toList());

    }

    private List<Version> findVersionsToExternalize(final Version version) {
        LOGGER.debug("Externalizing version with uri {} and its references", version.getItem().getUri().toString());
        List<Item> items = itemRepo.find(Item.Criteria
                .create()
                .interval(version.getInterval())
                .reference(version.getItem().getUri(), ReferenceType.COMPOSITION)
        );
        LOGGER.info("Found {} reference(s) that also needs to be externalized", items.size());
        List<Version> versions = items
                .stream()
                .flatMap(i -> i.getVersions(version.getInterval()).stream())
                .filter(Version::hasComposition)
                .filter(Version::isComplete)
                .collect(Collectors.toList());

        if (version.hasComposition() && version.isComplete()) {
            versions.add(version);
        }
        return versions;
    }

    private ExternalizationVisitor getExternalizationVisitor(final Bean<?> b) {
        CreationalContext<?> ctx = bm.createCreationalContext(b);
        ExternalizationVisitor v = (ExternalizationVisitor) bm.getReference(b, ExternalizationVisitor.class, ctx);
        return v;
    }

    private Bean<?> findAnyExternalizationVisitorBeans() {
        return bm.getBeans(ExternalizationVisitor.class, new AnnotationLiteral<Any>() {
        }).stream()
                .findAny()
                .orElseThrow(() -> new ExternalizationException("No externalization visitor found - a class implementing ExternalizationVisitor must "
                + "be pressent and annotated with ContentProcessingScoped abnd ContentProcessingMimeType"));
    }

    private List<ExternalFile> externalizeVersion(final Version version) {
        List<ExternalFile> results = new ArrayList<>(0);
        if (!processing(version.getUuid())) {
            LOGGER.info("Externalizing version {} with uri {}", version.getUuid(), version.getItem().getUri());
            List<TemporalReferenceTree<ExternalizationVisitor>> trees = new ArrayList<>();
            ContentContext.execute(() -> {
                Bean<?> bean = findAnyExternalizationVisitorBeans();
                Arrays.stream(bean.getBeanClass().getAnnotationsByType(ContentProcessingMimeType.class)).forEach(a -> {
                    if (version.getMimeType().matches(a)) {
                        ReferenceProcessor<ExternalizationVisitor> processor = new ReferenceProcessor<>(version, getExternalizationVisitor(bean));
                        trees.addAll(processor.process());
                    }
                });

            }, version.getContent());
            LOGGER.debug("Found {} files to externalize", trees.size());
            fileRepo.findAll(version.getInterval())
                    .stream()
                    .filter(f -> f.isDifferent(version))
                    .forEach(f -> {
                        LOGGER.debug("Deleting file {}", f.getUuid());
                        version.removeExternalizationId(f.getExternalizationId());
                        f.delete();
                    });

            trees.stream()
                    .map(t -> createExternalFile(t, version))
                    .filter(f -> f.isDifferent(version))
                    .forEach(f -> {
                        LOGGER.debug("Saving file {}", f.getUuid());
                        fileRepo.save(f);
                        results.add(f);
                    });

            processed(version.getUuid());
            scheduleService.reschedule();
        } else {
            LOGGER.info("Version {} with uri {} has already been externalized", version.getUuid(), version.getItem().getUri());
        }

        return results;
    }

    private ExternalFile createExternalFile(final TemporalReferenceTree<ExternalizationVisitor> t, final Version version) {
        ExternalFile.Builder builder = ExternalFile.builder()
                .item(version.getItem())
                .content(t.getVisitor().getContent())
                .interval(t.getInteval())
                .state(version.getState());
        t.getVisitor().getContentId().ifPresent(i -> builder.externalizationId(i));
        return builder.build();
    }

}
