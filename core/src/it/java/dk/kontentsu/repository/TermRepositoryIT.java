package dk.kontentsu.repository;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.EJBException;
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
            term = repo.get(term.getUuid());
            Set<UUID> delete = term.getChildren(true).stream().map(Term::getUuid).collect(Collectors.toSet());
            assertThat(delete.size(), greaterThanOrEqualTo(2));
            delete.forEach(repo::delete);
            repo.delete(term.getUuid());
            assertEquals(0, repo.findAll().size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() {
        repo.findAll();
    }

    @Test
    public void testCreate() throws Exception {
        try {
            userTransaction.begin();
            Term t = repo.create(new Item.URI("test1/test3/test3-name"));
            assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
            assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testCreateUsingStr() throws Exception {
        try {
            userTransaction.begin();
            Term t = repo.create("uri:/test1/test3/");
            assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
            assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testCreateUsingWrongStr() throws Exception {
        try {
            userTransaction.begin();
            repo.create("uri");
            fail("Must throw exception");
        } catch (EJBException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        } finally {
            userTransaction.rollback();
        }
    }

    @Test
    public void testFind() throws Exception {
        try {
            userTransaction.begin();
            Optional<Term> t = repo.find(term.getUuid());
            assertTrue(t.isPresent());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            List<Term> found = repo.findAll();
            assertEquals(1, found.size());
            assertEquals(2, found.get(0).getChildren(true).size());
        } finally {
            userTransaction.commit();
        }
    }

}
