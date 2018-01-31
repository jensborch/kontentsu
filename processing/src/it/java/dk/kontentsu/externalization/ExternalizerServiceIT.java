
package dk.kontentsu.externalization;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.model.Term;
import dk.kontentsu.model.Version;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.repository.TermRepository;
import dk.kontentsu.test.ContentTestData;
import dk.kontentsu.test.TestEJBContainer;
import dk.kontentsu.util.Transaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link ExternalizerService}.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalizerServiceIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private static EJBContainer container;
    private Item article1;
    private Term article1Path;
    private Item contact;
    private Term contactPath;
    private ContentTestData halJsonData;
    private ContentTestData jsonData;
    private Item page;
    private Term pagePath;
    private Version pageVersion;
    private ObjectMapper mapper;

    @Inject
    private ExternalizerService service;

    @Inject
    private ExternalFileRepository repo;

    @Inject
    private TermRepository termRepo;

    @Inject
    private EntityManager em;

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
        mapper = new ObjectMapper();
        halJsonData = new ContentTestData(ContentTestData.Type.HAL);
        jsonData = new ContentTestData(ContentTestData.Type.JSON);
    }

    public void createItems(final MimeType mimeType, ContentTestData data) throws Exception {
        try {
            userTransaction.begin();
            article1Path = termRepo.create(new Item.URI("items/article2/"));
            article1 = new Item(article1Path, mimeType);
            Version articleVersion1 = Version.builder()
                    .content(new Content(data.getArticle(1), StandardCharsets.UTF_8))
                    .from(NOW.minusDays(1000))
                    .to(NOW.plusDays(10))
                    .build();
            article1.addVersion(articleVersion1);

            Version articleVersion2 = Version.builder()
                    .content(new Content(data.getArticle(2), StandardCharsets.UTF_8))
                    .from(NOW.plusDays(15))
                    .to(NOW.plusDays(20))
                    .build();
            article1.addVersion(articleVersion2);

            contactPath = termRepo.create(new Item.URI("items/contact/"));
            contact = new Item(contactPath, mimeType);
            Version contactVersion = Version.builder()
                    .content(new Content(data.getContact(), StandardCharsets.UTF_8))
                    .from(NOW)
                    .build();
            contact.addVersion(contactVersion);

            pagePath = termRepo.create(new Item.URI("items/page-simple/"));
            page = new Item(pagePath, mimeType);
            pageVersion = Version.builder()
                    .content(new Content(data.getSimplePage(), StandardCharsets.UTF_8))
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
        em.remove(item);
    }

    @Test
    public void testDelete() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE, halJsonData);
        Content content = new Content("{\"test\": \"test\"}".getBytes(), Charset.defaultCharset());
        ExternalFile toDelete = ExternalFile.builder()
                .externalizationId("42")
                .content(content)
                .item(page)
                .from(NOW)
                .build();
        Transaction.create(userTransaction).param(toDelete).apply(f -> repo.save(f));

        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();
        assertEquals(2, result.size());
        ExternalFile deleted = Transaction.create(userTransaction).param(toDelete.getUuid()).apply(f -> repo.get(f));
        assertTrue(deleted.isDeleted());
    }

    @Test
    public void testExternalizeHalJson() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE, halJsonData);
        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();

        assertEquals(2, result.size());
        String external = result.get(0).getContent().getData();

        assertEquals(mapper.readTree(halJsonData.getSimplePageResults(1)), mapper.readTree(external));
        assertEquals(new Interval(NOW, NOW.plusDays(10)), result.get(0).getInterval());

        external = result.get(1).getContent().getData();
        assertEquals(mapper.readTree(halJsonData.getSimplePageResults(2)), mapper.readTree(external));
        assertEquals(new Interval(NOW.plusDays(15), NOW.plusDays(20)), result.get(1).getInterval());
    }

    @Test
    public void testExternalizeJson() throws Exception {
        createItems(MimeType.APPLICATION_JSON_TYPE, jsonData);
        List<ExternalFile> result = service.externalize(pageVersion.getUuid()).get();

        assertEquals(2, result.size());
        String external = result.get(0).getContent().getData();

        assertEquals(mapper.readTree(jsonData.getSimplePageResults(1)), mapper.readTree(external));
        assertEquals(new Interval(NOW, NOW.plusDays(10)), result.get(0).getInterval());

        external = result.get(1).getContent().getData();
        assertEquals(mapper.readTree(jsonData.getSimplePageResults(2)), mapper.readTree(external));
        assertEquals(new Interval(NOW.plusDays(15), NOW.plusDays(20)), result.get(1).getInterval());
    }

    @Test
    public void testNewArticle() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE, halJsonData);
        Version articleVersion;
        try {
            userTransaction.begin();
            article1 = em.find(Item.class, article1.getId());
            articleVersion = Version.builder()
                    .content(new Content(halJsonData.getArticle(2), StandardCharsets.UTF_8))
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
        assertEquals(mapper.readTree(halJsonData.getSimplePageResults(2)), mapper.readTree(file.getContent().getData()));
    }

    @Test
    public void testShouldNotBeExternalized() throws Exception {
        createItems(MimeType.APPLICATION_HAL_JSON_TYPE, halJsonData);

        try {
            userTransaction.begin();
            page = em.find(Item.class, page.getId());

            pageVersion = Version.builder()
                    .content(new Content(halJsonData.getSimplePage(), StandardCharsets.UTF_8))
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
