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
import dk.kontentsu.repository.HostRepository;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.repository.TermRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jdk.jfr.SettingDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
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

    @AfterEach
    public void tearDown() throws Exception {
        hostRepo.delete(hosts[0].getUuid());
        hostRepo.delete(hosts[1].getUuid());
        assertTrue(hostRepo.findAll().isEmpty());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals(2, hostRepo.findAll().size());
    }

    @Test
    public void testGet() throws Exception {
        assertEquals(hosts[1], hostRepo.get(hosts[1].getUuid()));
    }

    @Test()
    public void testNotFound() throws Exception {
        NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.get(UUID.randomUUID()));
        assertEquals("No entity found for query", e.getMessage());
    }

    @Test
    public void testGetByName() throws Exception {
        assertEquals(hosts[1], hostRepo.getByName(hosts[1].getName()));
    }

    @Test
    public void testNotFoundByName() throws Exception {
        NoResultException e = assertThrows(NoResultException.class, () -> hostRepo.getByName("test test"));
        assertEquals("No entity found for query", e.getMessage());
    }
}
