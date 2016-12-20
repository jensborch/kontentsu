package dk.kontentsu.repository;

import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.repository.CategoryRepository;
import dk.kontentsu.repository.TaxonomyRepository;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.Taxon;
import dk.kontentsu.model.Taxonomy;
import dk.kontentsu.model.internal.Item;
import dk.kontentsu.model.internal.Version;
import dk.kontentsu.test.TestEJBContainer;

/**
 * Integration test for {@link CategoryRepository}.
 *
 * @author Jens Borch Christiansen
 */
public class CategoryRepositoryIT {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private static EJBContainer container;
    private Taxonomy taxonomy;
    private Taxon category1;
    private Taxon category2;
    private SemanticUriPath path;

    @Inject
    private TaxonomyRepository taxRepo;

    @Inject
    private CategoryRepository catRepo;

    @Inject
    private ItemRepository itemRepo;

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
            taxonomy = taxRepo.save(new Taxonomy("Taxonomy"));
            category2 = (Taxon) catRepo.save(new Taxon(taxonomy, "test1", "test2"));
            category1 = (Taxon) catRepo.save(new Taxon(taxonomy, "test3", "test4"));
            path = new SemanticUriPath("uri1", "uri2");
            catRepo.save(path);
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        TestEJBContainer.inject(container, this);
        try {
            userTransaction.begin();
            catRepo.delete(category1.getUuid());
            catRepo.delete(category2.getUuid());
            catRepo.delete(path.getUuid());
            taxRepo.delete(taxonomy.getUuid());
        } finally {
            userTransaction.commit();
        }
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testNoTransaction() throws Exception {
        catRepo.findAll();
    }

    @Test
    public void testFindAll() throws Exception {
        try {
            userTransaction.begin();
            assertEquals(3, catRepo.findAll().size());
            assertEquals(0, catRepo.findActive().size());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testFindActive() throws Exception {
        try {
            userTransaction.begin();
            category1 = (Taxon) catRepo.get(category1.getUuid());
            path = (SemanticUriPath) catRepo.get(path.getUuid());
            Item item = new Item(new SemanticUri(path, "name1"));
            Version v = Version.builder()
                    .content(new Content("test".getBytes(), StandardCharsets.UTF_8, MimeType.parse("plain/txt")))
                    .build();
            item.addCategory(category1);
            item.addVersion(v);
            itemRepo.save(item);

            item = new Item(new SemanticUri(path, "name2"));
            v = Version.builder()
                    .content(new Content("test".getBytes(), StandardCharsets.UTF_8, MimeType.parse("plain/txt")))
                    .build();
            item.addCategory(category1);
            item.addVersion(v);
            itemRepo.save(item);

            List<SemanticUriPath> categories = catRepo.findActive();
            assertEquals(1, categories.size());
            assertEquals(2, ((SemanticUriPath) categories.get(0)).getVersionCount().intValue());
        } finally {
            userTransaction.rollback();
        }
    }

}
