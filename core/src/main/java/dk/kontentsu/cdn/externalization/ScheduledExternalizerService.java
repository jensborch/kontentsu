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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.internal.Host;
import dk.kontentsu.cdn.repository.ExternalFileRepository;

/**
 *
 * @author Jens Borch Christiansen
 */
@Singleton
@Startup
public class ScheduledExternalizerService {

    private static final int START_OFFSET = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledExternalizerService.class);

    private final AtomicBoolean first = new AtomicBoolean(true);

    @Resource
    private TimerService timerService;

    @Inject
    private ExternalFileRepository fileRepo;

    @Asynchronous
    public void reschedule() {
        List<ZonedDateTime> schedule = fileRepo.getSchedule();
        reschedule(schedule);
    }

    private void reschedule(final List<ZonedDateTime> schedule) {
        if (first.getAndSet(false)) {
            ZonedDateTime now = ZonedDateTime.now().plusMinutes(START_OFFSET);
            if (!schedule.contains(now)) {
                schedule.add(now);
            }
        }

        ScheduleExpression expression = new ScheduleExpression();
        for (ZonedDateTime dateTime : schedule) {

            ZonedDateTime t = dateTime.withZoneSameInstant(ZoneId.systemDefault());
            LOGGER.info("Files will be published at: " + t.format(DateTimeFormatter.ISO_DATE_TIME));
            expression
                    .second(t.getSecond())
                    .minute(t.getMinute())
                    .hour(t.getHour())
                    .dayOfMonth(t.getDayOfMonth())
                    .month(t.getMonthValue())
                    .year(t.getYear());

            timerService.createCalendarTimer(expression);
        }
    }

    @Timeout
    public void execute(final Timer timer) {
        ZonedDateTime time = getZonedDateTimeFromExpression(timer.getSchedule());
        LOGGER.info("Publishing externalised files available at: {}", time.format(DateTimeFormatter.ISO_DATE_TIME));
        List<ExternalFile> all = fileRepo.findAll(time);
        LOGGER.info("Found {} files to publish", all.size());
        Map<Host, Set<ExternalFile>> filesMap = new HashMap<>();
        all.stream().forEach(f -> {
            f.getItem().getHosts().stream().forEach(h -> {
                filesMap.putIfAbsent(h, new HashSet<>());
                filesMap.get(h).add(f);
            });
        });

        filesMap.forEach((host, files) -> {
            deleteAll(host, files);
            publishAll(host, files);
        });
    }

    private void deleteAll(final Host host, final Set<ExternalFile> files) {
        try {
            Files.walk(host.getPath()).filter(p -> p.toFile().isFile()).forEach(fsPath -> {
                if (!files.stream().map(f -> f.resolvePath(host.getPath())).anyMatch(p -> p.equals(fsPath))) {
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

    private void publishAll(final Host host, final Set<ExternalFile> files) {
        files.forEach(f -> publish(f, host.getPath()));
    }

    private void publish(final ExternalFile f, final Path hostPath) {
        Path filePath = hostPath.resolve(f.getItem().getUri().toPath());
        try {
            LOGGER.debug("Saving content to: " + filePath.toString());
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
            Files.copy(f.getContent().getDataAsBinaryStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.error("Failed to write CDN file: " + filePath.toString(), ex);
        }
    }

    private ZonedDateTime getZonedDateTimeFromExpression(final ScheduleExpression expression) {
        return ZonedDateTime.of(LocalDateTime
                .of(
                        Integer.parseInt(expression.getYear()),
                        Month.of(Integer.parseInt(expression.getMonth())),
                        Integer.parseInt(expression.getDayOfMonth()),
                        Integer.parseInt(expression.getHour()),
                        Integer.parseInt(expression.getMinute()),
                        Integer.parseInt(expression.getSecond())),
                ZoneId.systemDefault());
    }

}
