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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.model.internal.ReferenceProcessor;
import dk.kontentsu.cdn.model.internal.ReferenceType;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTree;
import dk.kontentsu.cdn.model.internal.Version;
import dk.kontentsu.cdn.repository.ExternalFileRepository;
import dk.kontentsu.cdn.repository.ItemRepository;

/**
 * Service facade for externalizing internal content.
 *
 * @author Jens Borch Christiansen
 */
@LocalBean
@Singleton
public class ExternalizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalizerService.class);

    private List<UUID> processing;
    private List<UUID> processed;

    @Inject
    private ExternalFileRepository fileRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private ScheduledExternalizerService scheduleService;

    @PostConstruct
    void init() {
        this.processed = new ArrayList<>();
        this.processing = new ArrayList<>();
    }

    private boolean processing(final UUID uuid) {
        boolean result = processing.contains(uuid) || processed.contains(uuid);
        if (!result) {
            processing.add(uuid);
        }
        return result;
    }

    private void processed(final UUID uuid) {
        processing.remove(uuid);
        processed.add(uuid);
        //TODO: cleanup list and/or create week list
    }

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Lock(LockType.READ)
    public Future<List<ExternalFile>> externalize(final UUID version) {
        Version v = itemRepo.getVersion(version);
        LOGGER.info("Externalizing version {} and its references", version);
        return new AsyncResult<>(externalize(v));

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Lock(LockType.WRITE)
    public List<ExternalFile> externalize(final Version version) {
        List<Version> versions = findVersionsToExternalize(version);
        return versions.stream()
                .flatMap(v -> externalizeVersion(v).stream())
                .sorted((f1, f2) -> f1.getInterval().getFrom().compareTo(f2.getInterval().getFrom()))
                .collect(Collectors.toList());

    }

    private List<Version> findVersionsToExternalize(final Version version) {
        LOGGER.debug("Externalizing version with uri {} and its references", version.getItem().getUri().toString());
        List<Item> items = itemRepo.find(ItemRepository.Criteria
                .create()
                .interval(version.getInterval())
                .referenceType(ReferenceType.COMPOSITION)
                .reference(version.getItem().getUri())
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

    private List<ExternalFile> externalizeVersion(final Version version) {
        List<ExternalFile> results = new ArrayList<>(0);
        if (!processing(version.getUuid())) {
            LOGGER.info("Externalizing version {} with uri {}", version.getUuid(), version.getItem().getUri());
            ReferenceProcessor<ExternalizationVisitor> processor = new ReferenceProcessor<>(version, ExternalizationVisitor.create(version));
            List<TemporalReferenceTree<ExternalizationVisitor>> trees = processor.process();
            results = trees.stream()
                    .map(t -> createExternalFile(t, version))
                    .collect(Collectors.toList());

            results.stream().forEach(f -> {
                LOGGER.debug("Saving file {}", f.getUuid());
                fileRepo.save(f);
            });
            processed(version.getUuid());
            scheduleService.reschedule();
        } else {
            LOGGER.info("Version {} with uri {} has already been externalized", version.getUuid(), version.getItem().getUri());
        }

        return results;
    }

    private ExternalFile createExternalFile(final TemporalReferenceTree<ExternalizationVisitor> t, final Version version) {
        return ExternalFile.builder()
                .item(version.getItem())
                .content(t.getVisitor().getContent())
                .interval(t.getInteval())
                .state(version.getState())
                .build();
    }

}
