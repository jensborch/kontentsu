package dk.kontentsu.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dk.kontentsu.model.Item;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.TermRepository;
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
@Transactional
@QuarkusTestResource(H2DatabaseTestResource.class)
public class TermRepositoryIT {

    @Inject
    TermRepository repo;

    private Term term;

    @BeforeEach
    public void setUp() throws Exception {
        term = repo.save(Term.create("uri"));
        term.append("/test1/test2");
    }

    @AfterEach
    public void tearDown() throws Exception {
        term = repo.get(term.getUuid());
        Set<UUID> delete = term.getChildren(true).stream().map(Term::getUuid).collect(Collectors.toSet());
        assertThat(delete.size(), greaterThanOrEqualTo(2));
        delete.forEach(repo::delete);
        repo.delete(term.getUuid());
        assertEquals(0, repo.findAll().size());
    }

    @Test
    public void testCreate() throws Exception {
        Term t = repo.create(new Item.URI("test1/test3/test3-name"));
        assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
        assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
    }

    @Test
    public void testPaths() throws Exception {
        assertEquals("uri:/", term.getPathWithTaxonomy());
        assertEquals("uri:/test1/", term.getChildren().stream().findAny().get().getPathWithTaxonomy());
        assertEquals("uri:/test1/test2/", term.getChildren().stream().findAny().get().getChildren().stream().findAny().get().getPathWithTaxonomy());
    }

    @Test
    public void testCreateUsingStr() throws Exception {
        Term t = repo.create("uri:/test1/test3/");
        assertEquals("uri:/test1/test3/", t.getPathWithTaxonomy());
        assertEquals(3, t.getParent().get().getParent().get().getChildren(true).size());
    }

    @Test
    public void testCreateUsingWrongStr() throws Exception {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> repo.create("uri"));
        assertEquals("Path 'uri' must match regular expression ^(?<tax>[\\p{L}\\d-]+):(?<term>\\/(?:[\\p{L}\\d-]+\\/)*)$", e.getMessage());
    }

    @Test
    public void testFind() throws Exception {
        Optional<Term> t = repo.find(term.getUuid());
        assertTrue(t.isPresent());
    }

    @Test
    public void testFindAll() throws Exception {
        List<Term> found = repo.findAll();
        assertEquals(1, found.size());
        assertEquals(2, found.get(0).getChildren(true).size());
    }

}
