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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Node;
import dk.kontentsu.repository.ExternalFileRepository;
import io.quarkus.arc.Arc;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for publishing externalized content.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationScoped
public class ScheduledExternalizerService {

    private static final int START_OFFSET = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledExternalizerService.class);

    private final AtomicBoolean first = new AtomicBoolean(true);

    @Inject
    Scheduler scheduler;

    @Inject
    ExternalFileRepository fileRepo;

    @Transactional
    public void reschedule() {
        Set<ZonedDateTime> schedule = fileRepo.getSchedule();
        reschedule(schedule);
    }

    private void reschedule(final Set<ZonedDateTime> schedule) {
        if (first.getAndSet(false)) {
            ZonedDateTime now = ZonedDateTime.now().plusMinutes(START_OFFSET);
            if (!schedule.contains(now)) {
                schedule.add(now);
            }
        }

        for (ZonedDateTime dateTime : schedule) {
            var id = UUID.randomUUID().toString();
            JobDetail job = JobBuilder.newJob(ExternalizerJob.class)
                    .withIdentity("externalizerJob-" + id, "kontentsu")
                    .build();
            ZonedDateTime t = dateTime.withZoneSameInstant(ZoneId.systemDefault());
            LOGGER.info("Files will be published at: " + t.format(DateTimeFormatter.ISO_DATE_TIME));
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("externalizerJobTrigger" + id, "externalizerJobGroup")
                    .startNow()
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withRepeatCount(0)
                                    .withIntervalInMilliseconds(t.toInstant().toEpochMilli() - Instant.now().toEpochMilli()))
                    .build();
            try {
                scheduler.scheduleJob(job, trigger);
            } catch (SchedulerException ex) {
                LOGGER.error("Error scheduling publishing", ex);
            }
        }
    }

    public static class ExternalizerJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            Arc.container().instance(ScheduledExternalizerService.class).get().execute(context);
        }

    }

    @Transactional
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public void execute(JobExecutionContext context) {
        ZonedDateTime time = context.getFireTime().toInstant().atZone(ZoneId.systemDefault());
        LOGGER.info("Publishing externalised files available at: {}", time.format(DateTimeFormatter.ISO_DATE_TIME));
        List<ExternalFile> all = fileRepo.findAll(time);
        LOGGER.info("Found {} files to publish", all.size());
        Map<Node, Set<ExternalFile>> filesMap = new HashMap<>();
        all.forEach(f
                -> f.getItem().getHosts().forEach(h -> {
                    filesMap.putIfAbsent(h, new HashSet<>());
                    filesMap.get(h).add(f);
                })
        );

        filesMap.forEach((host, files) -> {
            deleteAll(host, files);
            publishAll(host, files);
        });
    }

    private void deleteAll(final Node host, final Set<ExternalFile> files) {
        try (Stream<Path> walker = Files.walk(host.getPath())) {
            walker.filter(p -> p.toFile().isFile()).forEach(fsPath -> {
                if (files.stream().map(f -> f.resolvePath(host.getPath())).noneMatch(p -> p.equals(fsPath))) {
                    delete(fsPath);
                }
            });
        } catch (IOException ex) {
            LOGGER.error("Unable to walk file tree for filesystem with path " + host.getPath().toString(), ex);
        }
    }

    private void delete(final Path path) {
        try {
            Files.delete(path);
        } catch (IOException ex) {
            LOGGER.warn("Unable to delete file with path {}", path.toString());
        }
    }

    private void publishAll(final Node host, final Set<ExternalFile> files) {
        files.forEach(f -> publish(f, host.getPath()));
    }

    private void publish(final ExternalFile f, final Path hostPath) {
        Path filePath = f.resolvePath(hostPath);
        try {
            LOGGER.debug("Saving content to: " + filePath.toString());
            if (hasParent(filePath)) {
                Files.createDirectories(filePath.getParent());
            }
            if (!filePath.toFile().exists()) {
                Files.createFile(filePath);
            }
            Files.copy(f.getContent().getDataAsBinaryStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.error("Failed to write CDN file: " + filePath.toString(), ex);
        }
    }

    private boolean hasParent(final Path file) {
        Path parent = file.getParent();
        return parent != null && !parent.toFile().exists();
    }

}
