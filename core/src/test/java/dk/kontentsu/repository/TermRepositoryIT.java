package dk.kontentsu.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Item;
import dk.kontentsu.model.Term;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link TermRepository}.
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class TermRepositoryIT {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private Term term;

    @Inject
    TermRepository repo;

    @Inject
    UserTransaction userTransaction;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            userTransaction.begin();
            term = repo.save(new Term("uri"));
            term.append("/test1/test2");
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
            term = repo.get(term.getUuid());
            Set<UUID> delete = term.getChildren(true).stream().map(Term::getUuid).collect(Collectors.toSet());
            assertThat(delete.size(), greaterThanOrEqualTo(2));
            delete.forEach(repo::delete);
            repo.delete(term.getUuid());
            assertEquals(0, repo.findAll().size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testNoTransaction() {
        TransactionalException e = assertThrows(TransactionalException.class, () -> repo.findAll());
        assertThat(e.getMessage(), endsWith("Transaction is required for invocation"));
    }

    @Test
    public void testCreate() throws Exception {
        try {
            userTransaction.begin();
            Term t = repo.create(new Item.URI("test1/test3/test3-name"));
            assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
            assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testCreateUsingStr() throws Exception {
        try {
            userTransaction.begin();
            Term t = repo.create("uri:/test1/test3/");
            assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
            assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

    @Test
    public void testCreateUsingWrongStr() throws Exception {
        try {
            userTransaction.begin();
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> repo.create("uri"));
            assertEquals("Path 'uri' must match regular expression ^(?<tax>[\\p{L}\\d-]+):(?<term>\\/(?:[\\p{L}\\d-]+\\/)*)$", e.getMessage());
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
            List<Term> found = repo.findAll();
            assertEquals(1, found.size());
            assertEquals(2, found.get(0).getChildren(true).size());
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            fail(e);
        }
    }

}
