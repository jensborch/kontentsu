package dk.kontentsu.repository;

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

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.test.TestEJBContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for {@link ExternalFileRepository}
 *
 * @author Jens Borch Christiansen
 */
public class ExternalFileRepositoryIT {

    private static final Item.URI URI = new Item.URI("test1/test2/");
    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final ZonedDateTime FROM = NOW.plusDays(42);
    private static final ZonedDateTime TO = NOW.plusDays(80);

    private static EJBContainer container;

    @Inject
    private ExternalFileRepository fileRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private TermRepository termRepo;

    @Resource
    private UserTransaction userTransaction;

    private ExternalFile file;

    @BeforeClass
    public static void setUpClass() {
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
        Term path = termRepo.create(URI);

        Item item = itemRepo.findByUri(URI)
                .orElseGet(()
                        -> itemRepo.save(
                        new Item(termRepo.find(path.getUuid()).orElse(path),
                                new MimeType("text", "plain")))
                );

        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset());
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
            fileRepo.findAll().forEach(ExternalFile::delete);
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
    public void testNoTransaction() {
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
            Optional<ExternalFile> tmpFile = fileRepo.findByUri(URI, (ZonedDateTime) null);
            assertFalse(tmpFile.isPresent());

            tmpFile = fileRepo.findByUri(URI, NOW.plusDays(43));
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
