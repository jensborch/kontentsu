package dk.kontentsu.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link HostRepository}.
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
@Transactional
@QuarkusTestResource(H2DatabaseTestResource.class)
public class HostRepositoryIT {

    @Inject
    HostRepository hostRepo;

    @Inject
    TermRepository catRepo;

    @Inject
    ItemRepository itemRepo;

    private final Host[] hosts = new Host[2];

    public void setUp() throws Exception {
        hosts[0] = hostRepo.save(new Host("name1", "test test", URI.create("ftp://myusername:mypassword@somehost/"), "cdn/upload"));
        hosts[1] = hostRepo.save(new Host("name2", "test test", URI.create("sftp://myusername:mypassword@somehost/"), "cdn/upload"));

        Item.URI uri = new Item.URI("test1/test2/");
        Term path = catRepo.create(uri);

        Item item = itemRepo.findByUri(uri)
                .orElseGet(()
                        -> itemRepo.save(new Item(path, MimeType.APPLICATION_JSON_TYPE))
                );
        item.addHost(hostRepo.getByName("name2"));
    }

    public void tearDown() throws Exception {
        hostRepo.delete(hosts[0].getUuid());
        hostRepo.delete(hosts[1].getUuid());
        assertTrue(hostRepo.findAll().isEmpty());
    }

    @Test
    public void testFindAll() throws Exception {
        setUp();
        assertEquals(2, hostRepo.findAll().size());
        tearDown();
    }

    @Test
    public void testGet() throws Exception {
        setUp();
        assertEquals(hosts[1], hostRepo.get(hosts[1].getUuid()));
        tearDown();
    }

    @Test()
    public void testNotFound() throws Exception {
        setUp();
        NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.get(UUID.randomUUID()));
        assertEquals("No entity found for query", e.getMessage());
        tearDown();

    }

    @Test
    public void testGetByName() throws Exception {
        setUp();
        assertEquals(hosts[1], hostRepo.getByName(hosts[1].getName()));
        tearDown();
    }

    @Test
    public void testNotFoundByName() throws Exception {
        setUp();
        NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.getByName("test test"));
        assertEquals("No entity found for query", e.getMessage());
        tearDown();
    }
}
