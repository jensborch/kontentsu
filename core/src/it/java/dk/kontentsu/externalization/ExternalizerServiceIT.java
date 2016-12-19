package dk.kontentsu.externalization;

import dk.kontentsu.externalization.ExternalizerService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.internal.Item;
import dk.kontentsu.model.internal.ReferenceType;
import dk.kontentsu.model.internal.Version;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.cdn.test.TestEJBContainer;
import dk.kontentsu.upload.ContentTestData;
import dk.kontentsu.util.Transactions;

/**
 * Test for {@link ExternalizerService}.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalizerServiceIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private static EJBContainer container;
    private Item article1;
    private SemanticUri articleUri1;
    private Item contact;
    private SemanticUri contactUri;
    private ContentTestData data;
    private Item page;
    private SemanticUri pageUri;
    private Version pageVersion;

    @Inject
    private ExternalizerService service;

    @Inject
    private ExternalFileRepository repo;

    @Inject
    private EntityManager em;

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
    }

    public void createItems(final MimeType mimeType) throws Exception {
        try {
            userTransaction.begin();
            data = new ContentTestData();
            articleUri1 = SemanticUri.parse("items/article2");
            article1 = new Item(articleUri1);
            Version articleVersion1 = Version.builder()
                    .content(new Content(data.getArticle(1), StandardCharsets.UTF_8, mimeType))
                    .from(NOW.minusDays(1000))
                    .to(NOW.plusDays(10))
                    .build();
            article1.addVersion(articleVersion1);

            Version articleVersion2 = Version.builder()
                    .content(new Content(data.getArticle(2), StandardCharsets.UTF_8, mimeType))
                    .from(NOW.plusDays(15))
                    .to(NOW.plusDays(20))
                    .build();
            article1.addVersion(articleVersion2);

            contactUri = SemanticUri.parse("items/contact");
            contact = new Item(contactUri);
            Version contactVersion = Version.builder()
                    .content(new Content(data.getContact(), StandardCharsets.UTF_8, mimeType))
                    .from(NOW)
                    .build();
            contact.addVersion(contactVersion);

            pageUri = SemanticUri.parse("items/page-simple");
            page = new Item(pageUri);
            pageVersion = Version.builder()
                    .content(new Content(data.getSimplePage(), StandardCharsets.UTF_8, mimeType))
                    .reference(article1, ReferenceType.COMPOSITION)
                    .reference(contact, ReferenceType.COMPOSITION)
                    .from(NOW)
                    .to(NOW.plusDays(100))
                    .build();
            page.addVersion(pageVersion);

            em.persist(article1);
            em.persist(contact);
            em.persist(page);
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            deleteItem(article1);
            deleteItem(contact);
            deleteItem(page);
        } finally {
            userTransaction.commit();
        }
    }

    private void deleteItem(Item item) {
        item = em.find(Item.class, item.getId());
        SemanticUriPath path = item.getUri().getPath();
        em.remove(item);
        em.remove(path);
    }

    @Test
    public void testDelete() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE);
        Content content = new Content("{\"test\": \"test\"}".getBytes(), Charset.defaultCharset(), new MimeType("application", "hal+json"));
        ExternalFile toDelete = ExternalFile.builder()
                .externalizationId("42")
                .content(content)
                .item(page)
                .from(NOW)
                .build();
        Transactions.commit(userTransaction).param(toDelete).apply(f -> repo.save(f));

        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();
        assertEquals(2, result.size());
        ExternalFile deleted = Transactions.commit(userTransaction).param(toDelete.getUuid()).apply(f -> repo.get(f));
        assertTrue(deleted.isDeleted());
    }

    @Test
    public void testExternalize() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE);
        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();

        assertEquals(2, result.size());
        String external = result.get(0).getContent().getData();
        assertEquals(data.getSimplePageResults(1), external);
        assertEquals(new Interval(NOW, NOW.plusDays(10)), result.get(0).getInterval());

        external = result.get(1).getContent().getData();
        assertEquals(data.getSimplePageResults(2), external);
        assertEquals(new Interval(NOW.plusDays(15), NOW.plusDays(20)), result.get(1).getInterval());
    }

    @Test
    public void testNewArticle() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE);
        Version articleVersion;
        try {
            userTransaction.begin();
            article1 = em.find(Item.class, article1.getId());
            articleVersion = Version.builder()
                    .content(new Content(data.getArticle(2), StandardCharsets.UTF_8, MimeType.APPLICATION_HAL_JSON_TYPE))
                    .from(NOW.plusDays(21))
                    .to(NOW.plusDays(25))
                    .build();
            article1.addVersion(articleVersion);
        } finally {
            userTransaction.commit();
        }
        List<ExternalFile> result = service.externalize(articleVersion.getUuid()).get();
        assertEquals(3, result.size());
        ExternalFile file = result.stream().filter(f -> f.getInterval().equals(new Interval(NOW.plusDays(21), NOW.plusDays(25)))).findAny().get();
        assertNotNull(file);
        assertEquals(data.getSimplePageResults(2), file.getContent().getData());
    }

    @Test
    public void testShouldNotBeExternalized() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE);

        try {
            userTransaction.begin();
            page = em.find(Item.class, page.getId());

            pageVersion = Version.builder()
                    .content(new Content(data.getSimplePage(), StandardCharsets.UTF_8, MimeType.APPLICATION_HAL_JSON_TYPE))
                    .reference(article1, ReferenceType.COMPOSITION)
                    .reference(contact, ReferenceType.COMPOSITION)
                    .from(NOW.plusDays(101))
                    .to(NOW.plusDays(1000))
                    .build();
            page.addVersion(pageVersion);

        } finally {
            userTransaction.commit();
        }
        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();
        assertEquals(0, result.size());
    }

}
