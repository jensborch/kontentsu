package dk.kontentsu.repository;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Item;
import dk.kontentsu.model.Term;
import dk.kontentsu.test.TestEJBContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for {@link TermRepository}.
 *
 * @author Jens Borch Christiansen
 */
public class TermRepositoryIT {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private static EJBContainer container;

    private Term term;

    @Inject
    private TermRepository repo;

    @Resource
    private UserTransaction userTransaction;

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
            term = repo.save(new Term("uri"));
            term.append("/test1/test2");
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        TestEJBContainer.inject(container, this);
        try {
            userTransaction.begin();
            Set<UUID> delete = term.getChildren(true).stream().map(Term::getUuid).collect(Collectors.toSet());
            //assertEquals(2, delete.size());
            delete.forEach(repo::delete);
            repo.delete(term.getUuid());
            //assertEquals(0, repo.findAll().size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        repo.findAll();
    }

    @Test
    public void testCreate() throws Exception {
        try {
            userTransaction.begin();
            Term t = repo.create(new Item.URI("/test1/test3/name"));
            assertEquals("uri:/test1/test3/", t.getFullPath());
            //assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(3, repo.findAll().size());
        } finally {
            userTransaction.commit();
        }
    }



}
