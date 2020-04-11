package dk.kontentsu.repository;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
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
    private TermRepository repo;

    @Inject
    private UserTransaction userTransaction;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            userTransaction.begin();
            term = repo.save(new Term("uri"));
            term.append("/test1/test2");
        } finally {
            userTransaction.commit();
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
        } finally {
            userTransaction.commit();
        }
    }

    @Test
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
        } catch (Exception e) {
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
