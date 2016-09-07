package dk.kontentsu.cdn.repository;

import dk.kontentsu.cdn.repository.CategoryRepository;
import dk.kontentsu.cdn.repository.ItemRepository;
import dk.kontentsu.cdn.repository.ExternalFileRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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
import dk.kontentsu.cdn.model.MimeType;
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
                .interval(new Interval(NOW.plusDays(42), NOW.plusDays(80)))
                .item(item)
                .build();

        fileRepo.save(file);

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
            assertEquals(1, files.size());
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
            assertEquals(0, result.size());
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testFindByUri() throws Exception {
        try {
            userTransaction.begin();
            Optional<ExternalFile> file = fileRepo.findByUri(uri, null);
            assertFalse(file.isPresent());

            file = fileRepo.findByUri(uri, NOW.plusDays(43));
            assertTrue(file.isPresent());

        } finally {
            userTransaction.commit();
        }
    }

}
