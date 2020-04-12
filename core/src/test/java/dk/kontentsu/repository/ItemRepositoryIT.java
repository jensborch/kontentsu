package dk.kontentsu.repository;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.model.State;
import dk.kontentsu.model.Term;
import dk.kontentsu.model.Version;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link ItemRepository}
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ItemRepositoryIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Inject
    ItemRepository itemRepo;

    @Inject
    TermRepository termRepo;

    @Inject
    UserTransaction userTransaction;

    private Item item;
    private Term path;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            userTransaction.begin();
            path = termRepo.create(new Item.URI("test1/test2/"));
            item = create("test", NOW, Interval.INFINITE);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            List<Item> items = itemRepo.findAll();
            items.forEach(Item::delete);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    private Item create(final String edition, final ZonedDateTime from, final ZonedDateTime to) {
        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset());

        Version version = Version.builder()
                .content(content)
                .from(from)
                .to(to)
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .build();

        String name = (edition == null) ? "" : "test2-" + edition;
        Item i = itemRepo.findByUri(new Item.URI("test1/test2/" + name), State.ACTIVE, State.DELETED, State.DRAFT)
                .orElseGet(()
                        -> itemRepo.save(
                        new Item(termRepo.find(path.getUuid()).orElse(path),
                                edition,
                                new MimeType("text", "plain")))
                );

        i.addVersion(version);
        itemRepo.save(i);
        return i;
    }

    @Test
    public void testMultipleVersions() throws Exception {
        try {
            userTransaction.begin();
            create("test1", NOW.minusDays(10), NOW.minusDays(5));
            create("test1", NOW.minusDays(4), NOW.minusDays(1));
            List<Item> items = itemRepo.findAll();
            assertEquals(2, items.size());
            assertEquals(2, items.stream().filter(i -> i.getEdition().orElse("").equals("test1")).findFirst().get().getVersions().size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testNoTransaction() {
        TransactionalException e = assertThrows(TransactionalException.class, () -> itemRepo.findAll());
        assertThat(e.getMessage(), endsWith("Transaction is required for invocation"));
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
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
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
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindByUri() throws Exception {
        try {
            userTransaction.begin();
            Optional<Item> result = itemRepo.findByUri(new Item.URI("test1/test2/test2-test"));
            assertTrue(result.isPresent());
            assertEquals(item, result.get());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindByUriNoEdition() throws Exception {
        try {
            userTransaction.begin();
            Item i = create(null, NOW.minusDays(10), NOW.minusDays(5));
            Optional<Item> result = itemRepo.findByUri(new Item.URI("test1/test2/"));
            assertTrue(result.isPresent());
            assertEquals(i, result.get());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindByCriteria() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
            result = itemRepo.find(Item.Criteria.create().at(NOW.plusSeconds(1)));
            assertNotNull(result);
            assertEquals(1, result.size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindByCriteriaIntervalOverlap() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
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
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindByCriteriaIntervalIntersects() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
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
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testFindItems() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
                    .mineType(new MimeType("text", "plain"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(1, result.size());

            result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
                    .edition("test")
                    .interval(new Interval(NOW.plusSeconds(10), NOW.plusSeconds(60))));
            assertNotNull(result);
            assertEquals(1, result.size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testNotFoundByCriteria() throws Exception {
        try {
            userTransaction.begin();
            List<Item> result = itemRepo.find(Item.Criteria.create()
                    .term("uri:/test1/test2/")
                    .mineType(new MimeType("text", "non"))
                    .at(NOW.plusDays(1)));
            assertNotNull(result);
            assertEquals(0, result.size());
            result = itemRepo.find(Item.Criteria.create().at(NOW.minusDays(100)));
            assertNotNull(result);
            assertEquals(0, result.size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testNonExistingComposition() throws Exception {
        try {
            userTransaction.begin();
            Content content = new Content("Composition".getBytes(), Charset.defaultCharset());
            Term foundPath = termRepo.get(path.getUuid());
            Item doNotExistItem = new Item(foundPath, "do-not-exist", new MimeType("text", "plain"));
            Item compItem = new Item(foundPath, "composition", new MimeType("text", "plain"));
            Version compVersion = Version.builder()
                    .from(NOW.minusHours(24))
                    .to(NOW)
                    .content(content)
                    .reference(doNotExistItem, ReferenceType.COMPOSITION)
                    .build();
            compItem.addVersion(compVersion);
            itemRepo.save(compItem);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testOverlap() throws Exception {
        try {
            userTransaction.begin();
            Term foundPath = termRepo.get(path.getUuid());
            Content content = new Content("Overlap".getBytes(), Charset.defaultCharset());
            Item overlap = new Item(foundPath, "test", new MimeType("text", "plain"));
            Version version = Version.builder()
                    .from(NOW.plusDays(2))
                    .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                    .content(content)
                    .build();
            overlap.addVersion(version);

            catchException(itemRepo).save(overlap);
            assertTrue(caughtException().getCause() instanceof PersistenceException);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
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
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }
}
