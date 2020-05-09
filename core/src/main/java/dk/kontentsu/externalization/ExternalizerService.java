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
package dk.kontentsu.externalization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

import javax.annotation.PostConstruct;

import javax.transaction.Transactional;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.model.Version;
import dk.kontentsu.spi.InjectableContentProcessingScope;
import dk.kontentsu.model.processing.ReferenceProcessor;
import dk.kontentsu.model.processing.TemporalReferenceTree;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.spi.ContentProcessingMimeType;

import java.util.EnumMap;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.inject.Instance;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service facade for externalizing internal content.
 *
 * @author Jens Borch Christiansen
 */
@Singleton
@Transactional
public class ExternalizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalizerService.class);

    private final Set<UUID> processing = new HashSet<>();

    @Inject
    InjectableContentProcessingScope scope;

    @Inject
    ExternalFileRepository fileRepo;

    @Inject
    ItemRepository itemRepo;

    @Inject
    ScheduledExternalizerService scheduleService;

    @Inject
    BeanManager bm;

    @Inject
    Instance<ExternalizationVisitor> visitors;

    //private Map<MimeType, Bean<?>> externalizationVisitorBeans;

    /*@PostConstruct
    public void init() {
        externalizationVisitorBeans = getExternalizationVisitorBeansMap();
    }*/
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
    public Future<List<ExternalFile>> externalize(final UUID version) {
        Version v = itemRepo.getVersion(version);
        LOGGER.info("Externalizing version {} and its references", version);
        return CompletableFuture.completedFuture(externalize(v));

    }

    @Transactional(Transactional.TxType.MANDATORY)
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

        boolean defaultVisitor = getHighestPriorityExternalizationVisitorBean(version).map(Bean::getBeanClass).filter(DefaultExternalizationVisitor.class::equals).isPresent();

        if (defaultVisitor || (version.hasComposition() && version.isComplete())) {
            versions.add(version);
        }
        return versions;
    }

    @SuppressWarnings("unchecked")
    private ExternalizationVisitor getExternalizationVisitor(final Bean<?> b) {
        CreationalContext<?> ctx = bm.createCreationalContext(b);
        Object visitor = bm.getReference(b, ExternalizationVisitor.class, ctx);
        try {
            return (ExternalizationVisitor) visitor;
        } catch (ClassCastException e) {
            throw new ExternalizationException("Could not cast externalization visitor", e);
        }
    }

    private Set<Bean<?>> findAllExternalizationVisitorBeans() {
        return bm.getBeans(ExternalizationVisitor.class, new AnnotationLiteral<Any>() {
        });
    }

    private Map<MimeType, Bean<?>> getExternalizationVisitorBeansMap() {
        visitors.forEach(v -> LOGGER.info("{}", v));
        Map<MimeType, Bean<?>> map = new HashMap<>();
        findAllExternalizationVisitorBeans().forEach(b
                -> Arrays.stream(b.getBeanClass().getAnnotationsByType(ContentProcessingMimeType.class))
                        .forEach(a
                                -> Arrays.stream(a.value()).map(MimeType::parse)
                                .forEach(m -> map.put(m, b))
                        )
        );
        LOGGER.debug("Found {} CDI externalization visitors", map.size());
        return map;
    }

    private Map<MimeType.Match, Bean<?>> getMatchingExternalizationVisitorBeans(final Version version) {
        Map<MimeType.Match, Bean<?>> matches = new EnumMap<>(MimeType.Match.class);
        getExternalizationVisitorBeansMap().forEach((key, value) -> {
            MimeType.Match m = key.matches(version.getMimeType());
            if (m.isMatch()) {
                matches.put(m, value);
            }
        });
        return matches;
    }

    private Optional<Bean<?>> getHighestPriorityExternalizationVisitorBean(final Version version) {
        return getMatchingExternalizationVisitorBeans(version).entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey().getPriority(), e1.getKey().getPriority()))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private List<ExternalFile> externalizeVersion(final Version version) {
        List<ExternalFile> results = new ArrayList<>(0);
        if (processing(version.getUuid())) {
            LOGGER.info("Version {} with uri {} has already been externalized", version.getUuid(), version.getItem().getUri());
        } else {
            LOGGER.info("Externalizing version {} with uri {}", version.getUuid(), version.getItem().getUri());
            List<TemporalReferenceTree<ExternalizationIdentifierVisitor.Results, ExternalizationIdentifierVisitor>> trees = new ArrayList<>();
            scope.execute(
                    () -> trees.addAll(externalizeVersionInScope(version)),
                    version.getContent());
            LOGGER.debug("Found {} files to externalize", trees.size());
            fileRepo.findByUri(version.getItem().getUri(), version.getInterval())
                    .stream()
                    .filter(f -> f.isDifferent(version))
                    .sorted((v1, v2) -> v1.getItem().getUri().compareTo(v2.getItem().getUri()))
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
        }
        return results;
    }

    private List<TemporalReferenceTree<ExternalizationIdentifierVisitor.Results, ExternalizationIdentifierVisitor>> externalizeVersionInScope(final Version version) {
        List<TemporalReferenceTree<ExternalizationIdentifierVisitor.Results, ExternalizationIdentifierVisitor>> trees = new ArrayList<>();
        Optional<Bean<?>> bean = getHighestPriorityExternalizationVisitorBean(version);
        if (bean.isPresent()) {
            ExternalizationIdentifierVisitor visitor = new ExternalizationIdentifierVisitor(getExternalizationVisitor(bean.get()));
            LOGGER.info("Using visitor {} to externalize version {} with mime type {}",
                    bean.get().getBeanClass().getCanonicalName(),
                    version.getUuid(),
                    version.getMimeType());
            ReferenceProcessor<ExternalizationIdentifierVisitor.Results, ExternalizationIdentifierVisitor> processor
                    = new ReferenceProcessor<>(version, visitor);
            trees.addAll(processor.process());
        } else {
            LOGGER.warn("No visitor found to externalizer version {} with mime type {}", version.getUuid(), version.getMimeType());
        }
        return trees;
    }

    private ExternalFile createExternalFile(final TemporalReferenceTree<ExternalizationIdentifierVisitor.Results, ExternalizationIdentifierVisitor> t, final Version version) {
        ExternalFile.Builder builder = ExternalFile.builder()
                .item(version.getItem())
                .content(t.getResult().getContent())
                .interval(t.getInterval())
                .externalizationId(t.getResult().getId())
                .state(version.getState());
        return builder.build();
    }

}
