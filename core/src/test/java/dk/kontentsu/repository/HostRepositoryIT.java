package dk.kontentsu.repository;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link HostRepository}.
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class HostRepositoryIT {

    @Inject
    HostRepository hostRepo;

    @Inject
    TermRepository catRepo;

    @Inject
    ItemRepository itemRepo;

    @Inject
    UserTransaction userTransaction;

    private final Host[] hosts = new Host[2];

    @BeforeEach
    public void setUp() throws Exception {
        try {
            userTransaction.begin();
            hosts[0] = hostRepo.save(new Host("name1", "test test", URI.create("ftp://myusername:mypassword@somehost/"), "cdn/upload"));
            hosts[1] = hostRepo.save(new Host("name2", "test test", URI.create("sftp://myusername:mypassword@somehost/"), "cdn/upload"));

            Item.URI uri = new Item.URI("test1/test2/");
            Term path = catRepo.create(uri);

            Item item = itemRepo.findByUri(uri)
                    .orElseGet(()
                            -> itemRepo.save(new Item(path, MimeType.APPLICATION_JSON_TYPE))
                    );
            item.addHost(hostRepo.getByName("name2"));
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
            hostRepo.delete(hosts[0].getUuid());
            hostRepo.delete(hosts[1].getUuid());
            assertTrue(hostRepo.findAll().isEmpty());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }

    }

    @Test
    public void testNoTransaction() {
        TransactionalException e = assertThrows(TransactionalException.class, () -> hostRepo.findAll());
        assertThat(e.getMessage(), endsWith("Transaction is required for invocation"));
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(2, hostRepo.findAll().size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testGet() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(hosts[1], hostRepo.get(hosts[1].getUuid()));
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test()
    public void testNotFound() throws Exception {
        try {
            userTransaction.begin();
            NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.get(UUID.randomUUID()));
            assertEquals("No entity found for query", e.getMessage());
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testGetByName() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(hosts[1], hostRepo.getByName(hosts[1].getName()));
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testNotFoundByName() throws Exception {
        try {
            userTransaction.begin();
            NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.getByName("test test"));
            assertEquals("No entity found for query", e.getMessage());
        } finally {
            userTransaction.rollback();
        }
    }
}
