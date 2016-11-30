package dk.kontentsu.cdn.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.spi.MimeType;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.test.TestEJBContainer;

/**
 * Integration test for {@link ExternalFileRepository}
 *
 * @author Jens Borch Christiansen
 */
public class ExternalFileRepositoryIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final ZonedDateTime FROM = NOW.plusDays(42);
    private static final ZonedDateTime TO = NOW.plusDays(80);

    private static EJBContainer container;

    @Inject
    private ExternalFileRepository fileRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private CategoryRepository catRepo;
    private SemanticUri uri;

    @Resource
    private UserTransaction userTransaction;

    private SemanticUriPath path;
    private ExternalFile file;

    @BeforeClass
    public static void setUpClass() throws Exception {
        container = TestEJBContainer.create();
    }

    @AfterClass
    public static void tearDownClass() {
        if (container != null) {
            container.close();
        }
    }

    @Before
    public void setUp() throws Exception {
        TestEJBContainer.inject(container, this);
        userTransaction.begin();
        SemanticUriPath tmpPath = new SemanticUriPath("test1", "test2");
        path = catRepo.findByUri(tmpPath).orElseGet(() -> {
            return (SemanticUriPath) catRepo.save(tmpPath);
        });

        uri = new SemanticUri(path, "test");

        Item item = itemRepo.findByUri(uri).orElseGet(() -> {
            return itemRepo.save(new Item(uri));
        });

        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));
        file = ExternalFile.builder()
                .content(content)
                .interval(new Interval(FROM, TO))
                .item(item)
                .build();

        fileRepo.save(file);

        fileRepo.save(ExternalFile.builder()
                .content(content)
                .interval(new Interval(FROM.minusHours(12), FROM))
                .item(item)
                .build());

        fileRepo.save(ExternalFile.builder()
                .content(content)
                .interval(new Interval(TO, TO.plusMinutes(4)))
                .item(item)
                .build());

        fileRepo.save(ExternalFile.builder()
                .content(content)
                .interval(new Interval(NOW.minusDays(2), NOW.minusDays(1)))
                .item(item)
                .build());

        userTransaction.commit();
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            fileRepo.findAll().stream().forEach(f -> f.delete());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            List<ExternalFile> files = fileRepo.findAll();
            assertEquals(4, files.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindAllAtTime() throws Exception {
        try {
            userTransaction.begin();
            List<ExternalFile> files = fileRepo.findAll(NOW);
            assertEquals(0, files.size());
            files = fileRepo.findAll(NOW.plusDays(43));
            assertEquals(1, files.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindInInterval() throws Exception {
        try {
            userTransaction.begin();
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
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        fileRepo.findAll();
    }

    @Test
    public void testDelete() throws Exception {
        try {
            userTransaction.begin();
            file = fileRepo.get(file.getUuid());
            file.delete();
            List<ExternalFile> result = fileRepo.findAll();
            assertNotNull(result);
            assertEquals(3, result.size());
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testFindByUri() throws Exception {
        try {
            userTransaction.begin();
            Optional<ExternalFile> tmpFile = fileRepo.findByUri(uri, null);
            assertFalse(tmpFile.isPresent());

            tmpFile = fileRepo.findByUri(uri, NOW.plusDays(43));
            assertTrue(tmpFile.isPresent());

        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testGetSchedule() throws Exception {
        try {
            userTransaction.begin();
            Set<ZonedDateTime> result = fileRepo.getSchedule();
            assertEquals(3, result.size());
            assertTrue(result.contains(FROM.withZoneSameInstant(ZoneOffset.UTC)));
            assertTrue(result.contains(TO.plusMinutes(4).withZoneSameInstant(ZoneOffset.UTC)));
            assertTrue(result.contains(FROM.minusHours(12).withZoneSameInstant(ZoneOffset.UTC)));
        } finally {
            userTransaction.commit();
        }
    }

}
