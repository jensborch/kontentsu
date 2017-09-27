package dk.kontentsu.repository;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.Taxon;
import dk.kontentsu.model.Taxonomy;
import dk.kontentsu.model.Term;
import dk.kontentsu.model.Version;
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
            //.append("/test1/test2"));
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        TestEJBContainer.inject(container, this);
        try {
            userTransaction.begin();
            repo.delete(term.getUuid());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        repo.findAll();
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(1, repo.findAll().size());
        } finally {
            userTransaction.commit();
        }
    }



}
