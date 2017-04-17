package dk.kontentsu.repository;

import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.repository.CategoryRepository;
import dk.kontentsu.repository.HostRepository;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;
import dk.kontentsu.test.TestEJBContainer;

/**
 * Integration test for {@link HostRepository}.
 *
 * @author Jens Borch Christiansen
 */
public class HostRepositoryIT {

    private static EJBContainer container;

    @Inject
    private HostRepository hostRepo;

    @Inject
    private CategoryRepository catRepo;

    @Inject
    private ItemRepository itemRepo;

    @Resource
    private UserTransaction userTransaction;

    private final Host[] hosts = new Host[2];

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
        try {
            userTransaction.begin();
            hosts[0] = hostRepo.save(new Host("name1", "test test", URI.create("ftp://myusername:mypassword@somehost/"), "cdn/upload"));
            hosts[1] = hostRepo.save(new Host("name2", "test test", URI.create("sftp://myusername:mypassword@somehost/"), "cdn/upload"));

            SemanticUriPath tmpPath = new SemanticUriPath("test1", "test2");
            SemanticUriPath path = catRepo.findByUri(tmpPath).orElseGet(() -> {
                return (SemanticUriPath) catRepo.save(tmpPath);
            });
            Item item = itemRepo.findByUri(new SemanticUri(path, "test2"))
                    .orElseGet(() -> {
                        return itemRepo.save(new Item(new SemanticUri(path, "test2")));
                    });
            item.addHost(hostRepo.getByName("name2"));
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            hostRepo.delete(hosts[0].getUuid());
            hostRepo.delete(hosts[1].getUuid());
            assertTrue(hostRepo.findAll().isEmpty());
        } finally {
            userTransaction.commit();
        }

    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        hostRepo.findAll();
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(2, hostRepo.findAll().size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testGet() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(hosts[1], hostRepo.get(hosts[1].getUuid()));
        } finally {
            userTransaction.commit();
        }
    }

    @Test()
    public void testNotFound() throws Exception {
        try {
            userTransaction.begin();
            catchException(hostRepo).get(UUID.randomUUID());
            assertTrue(caughtException() instanceof EJBTransactionRolledbackException);
            assertTrue(caughtException().getCause() instanceof NoResultException);
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testGetByName() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(hosts[1], hostRepo.getByName(hosts[1].getName()));
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testNotFoundByName() throws Exception {
        try {
            userTransaction.begin();
            catchException(hostRepo).getByName("test test");
            assertTrue(caughtException() instanceof EJBTransactionRolledbackException);
            assertTrue(caughtException().getCause() instanceof NoResultException);
        } finally {
            userTransaction.rollback();
        }
    }
}
