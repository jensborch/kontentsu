package dk.kontentsu.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.repository.TermRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link ExternalFileRepository}
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
@Transactional
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ExternalFileRepositoryIT {

    private static final Item.URI URI = new Item.URI("test1/test2/");
    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final ZonedDateTime FROM = NOW.plusDays(42);
    private static final ZonedDateTime TO = NOW.plusDays(80);

    @Inject
    ExternalFileRepository fileRepo;

    @Inject
    ItemRepository itemRepo;

    @Inject
    TermRepository termRepo;

    private ExternalFile file;

    @BeforeEach
    public void setUp() throws Exception {
        Term path = termRepo.create(URI);

        Item item = itemRepo.findByUri(URI).orElseGet(() -> itemRepo
                .save(new Item(path, new MimeType("text", "plain"))));

        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset());
        file = ExternalFile.builder().content(content).interval(new Interval(FROM, TO)).item(item).build();

        fileRepo.save(file);

        fileRepo.save(ExternalFile.builder().content(content).interval(new Interval(FROM.minusHours(12), FROM))
                .item(item).build());

        fileRepo.save(ExternalFile.builder().content(content).interval(new Interval(TO, TO.plusMinutes(4))).item(item)
                .build());

        fileRepo.save(ExternalFile.builder().content(content).interval(new Interval(NOW.minusDays(2), NOW.minusDays(1)))
                .item(item).build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        fileRepo.findAll().forEach(ExternalFile::delete);
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals(1, termRepo.findAll().size());
        List<Item> items = itemRepo.findAll();
        assertEquals(1, items.size());
        List<ExternalFile> files = fileRepo.findAll();
        assertEquals(4, files.size());
    }

    @Test
    public void testFindAllAtTime() throws Exception {
        List<ExternalFile> files = fileRepo.findAll(NOW);
        assertEquals(0, files.size());
        files = fileRepo.findAll(NOW.plusDays(43));
        assertEquals(1, files.size());
    }

    @Test
    public void testFindInInterval() throws Exception {
        List<ExternalFile> files = fileRepo.findAll(new Interval(NOW, NOW.plusDays(10)));
        assertEquals(0, files.size());
        files = fileRepo.findAll(new Interval(NOW, NOW.plusDays(43)));
        assertEquals(2, files.size());
        files = fileRepo.findAll(new Interval(NOW.plusDays(81), NOW.plusDays(82)));
        assertEquals(0, files.size());
        files = fileRepo.findAll(new Interval(NOW.plusDays(50), NOW.plusDays(52)));
        assertEquals(1, files.size());
        files = fileRepo.findAll(new Interval(NOW.plusDays(50), NOW.plusDays(100)));
        assertEquals(2, files.size());
    }

    @Test
    public void testDelete() throws Exception {
        file = fileRepo.get(file.getUuid());
        file.delete();
        List<ExternalFile> result = fileRepo.findAll();
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testFindByUri() throws Exception {
        Optional<ExternalFile> tmpFile = fileRepo.findByUri(URI, (ZonedDateTime) null);
        assertFalse(tmpFile.isPresent());

        tmpFile = fileRepo.findByUri(URI, NOW.plusDays(43));
        assertTrue(tmpFile.isPresent());
    }

    @Test
    public void testGetSchedule() throws Exception {
        Set<Instant> result = fileRepo.getSchedule().stream().map(ZonedDateTime::toInstant)
                .collect(Collectors.toSet());
        assertEquals(3, result.size());
        assertThat(result, containsInAnyOrder(FROM.toInstant(), TO.plusMinutes(4).toInstant(), FROM.minusHours(12).toInstant()));
    }

}
