package dk.kontentsu.repository;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.Version;
import dk.kontentsu.test.TestEJBContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for {@link ItemRepository}
 *
 * @author Jens Borch Christiansen
 */
    public class ItemRepositoryIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private static EJBContainer container;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private CategoryRepository catRepo;

    @Resource
    private UserTransaction userTransaction;

    private Item item;
    private SemanticUriPath path;

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

        item = create("test", NOW, Interval.INFINITE);
        userTransaction.commit();
    }

    @After
    public void tearDown() throws Exception {
        userTransaction.begin();
        List<Item> items = itemRepo.findAll();
        items.forEach(Item::delete);
        userTransaction.commit();
    }

    private Item create(final String name, final ZonedDateTime from, final ZonedDateTime to) throws Exception {
        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));

        Version version = Version.builder()
                .content(content)
                .from(from)
                .to(to)
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .build();

        String itemName = (name == null) ? "test" : name;
        Item tmpItem = itemRepo.findByUri(new SemanticUri(path, itemName))
                .orElseGet(() -> {
                    path = (SemanticUriPath) catRepo.get(path.getUuid());
                    Item i = new Item(new SemanticUri(path, itemName));
                    return itemRepo.save(i);
                });

        tmpItem.addVersion(version);
        return tmpItem;
    }

    @Test
    public void testMultipleVersions() throws Exception {
        try {
            userTransaction.begin();
            create("test1", NOW.minusDays(10), NOW.minusDays(5));
            create("test1", NOW.minusDays(4), NOW.minusDays(1));
            List<Item> items = itemRepo.findAll();
            assertEquals(2, items.size());
            assertEquals(2, items.stream().filter(i -> i.getName().equals("test1")).findFirst().get().getVersions().size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        itemRepo.findAll();
    }

    @Test
    public void testDelete() throws Exception {
        try {
            userTransaction.begin();
            item = itemRepo.get(item.getUuid());
            item.delete();
            List<Item> result = itemRepo.findAll();
            assertNotNull(result);
            assertEquals(0, result.size());
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.findAll();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(item, result.get(0));
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindByCriteria() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .path(new SemanticUriPath("test1", "test2"))
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
            result = itemRepo.find(Item.Criteria.create().at(NOW.plusSeconds(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindByCriteriaIntervalOverlap() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .path(new SemanticUriPath("test1", "test2"))
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
            result = itemRepo.find(Item.Criteria
                    .create()
                    .from(NOW.minusSeconds(60))
                    .to(NOW.plusSeconds(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindByCriteriaIntervalIntersects() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .path(new SemanticUriPath("test1", "test2"))
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
            result = itemRepo.find(Item.Criteria
                    .create()
                    .from(NOW.plusSeconds(10))
                    .to(NOW.plusSeconds(60)));
            assertNotNull(result);
            assertEquals(1, result.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindItems() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .path(new SemanticUriPath("test1", "test2"))
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());

            result = itemRepo.find(Item.Criteria.create()
                    .uri(new SemanticUri(new SemanticUriPath("test1", "test2"), "test"))
                    .interval(new Interval(NOW.plusSeconds(10), NOW.plusSeconds(60))));
            assertNotNull(result);
            assertEquals(1, result.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testNotFoundByCriteria() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .path(new SemanticUriPath("test1", "test2"))
                    .mineType(new MimeType("text", "non"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(0, result.size());
            result = itemRepo.find(Item.Criteria.create().at(NOW.minusDays(100)));
            assertNotNull(result);
            assertEquals(0, result.size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = RollbackException.class)
    public void testNonExistingComposition() throws Exception {
        try {
            userTransaction.begin();
            Content content = new Content("Composition".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));
            SemanticUriPath foundPath = (SemanticUriPath) catRepo.get(path.getUuid());
            Item doNotExistItem = new Item(new SemanticUri(foundPath, "do-not-exist"));
            Item compItem = new Item(new SemanticUri(foundPath, "composition"));
            Version compVersion = Version.builder()
                    .from(NOW.minusHours(24))
                    .to(NOW)
                    .content(content)
                    .reference(doNotExistItem, ReferenceType.COMPOSITION)
                    .build();
            compItem.addVersion(compVersion);
            itemRepo.save(compItem);
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testOverlap() throws Exception {
        try {
            userTransaction.begin();
            SemanticUriPath foundPath = (SemanticUriPath) catRepo.get(path.getUuid());
            Content content = new Content("Overlap".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));
            Item overlap = new Item(new SemanticUri(foundPath, "test"));
            Version version = Version.builder()
                    .from(NOW.plusDays(2))
                    .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                    .content(content)
                    .build();
            overlap.addVersion(version);

            catchException(itemRepo).save(overlap);
            assertTrue(caughtException() instanceof EJBTransactionRolledbackException);
            assertTrue(caughtException().getCause() instanceof PersistenceException);

        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testSaveContent() throws Exception {
        try {
            userTransaction.begin();
            item = itemRepo.get(item.getUuid());

            Content content = itemRepo.saveContent(new ByteArrayInputStream(new byte[]{'1'}), StandardCharsets.UTF_8, MimeType.IMAGE_ANY_TYPE);

            Version version = Version.builder()
                    .from(NOW.minusDays(100))
                    .to(NOW.minusDays(99))
                    .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                    .content(content)
                    .build();
            item.addVersion(version);

            assertNotNull(content.getData());
            assertEquals(1, content.getSize());
        } finally {
            userTransaction.rollback();
        }
    }

}
