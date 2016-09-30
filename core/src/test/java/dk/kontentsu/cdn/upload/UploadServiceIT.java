package dk.kontentsu.cdn.upload;

import static com.googlecode.catchexception.CatchException.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.internal.Host;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.repository.HostRepository;
import dk.kontentsu.cdn.repository.ItemRepository;
import dk.kontentsu.cdn.test.TestEJBContainer;

/**
 * Test for {@link dk.kontentsu.cdn.service.UploadService}
 *
 * @author Jens Borch Christiansen
 */
public class UploadServiceIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private static EJBContainer container;
    private static ContentTestData data;
    private Host host;

    @Inject
    private HostRepository hostRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private UploadService service;

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
        data = new ContentTestData();
        try {
            userTransaction.begin();
            host = hostRepo.findByName("test_host").orElseGet(() -> {
                return hostRepo.save(new Host("test_host", "Test description", URI.create("ftp://myusername:mypassword@somehost/"), "cdn/upload"));
            });
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            itemRepo.findAll().stream().forEach(i -> i.delete());
        } finally {
            userTransaction.commit();
        }

    }

    @Test
    public void testUploadPlainText() throws Exception {
        InputStream is = new ByteArrayInputStream("test data".getBytes());
        UploadItem uploadeItem = UploadItem.builder()
                .content("TestRef", is)
                .uri(SemanticUri.parse("test/test/name"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("text", "plain"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        Item result = service.upload(uploadeItem);
        assertEquals("name", result.getName());
        assertEquals(1, result.getVersions().size());
        assertEquals(1, result.getVersions().stream().filter(v -> v.getInterval().getFrom().equals(NOW)).collect(Collectors.counting()).intValue());
        assertEquals(1, result.getVersions().stream().filter(v -> v.getInterval().getTo().equals(Interval.INFINIT)).collect(Collectors.counting()).intValue());
        assertEquals(1, result.getHosts().size());
        assertEquals(host, result.getHosts().stream().findFirst().get());
    }

    @Test
    public void testSimplePage() throws Exception {
        UploadItem uploadeItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(SemanticUri.parse("items/article2"))
                .interval(new Interval())
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        Item result = service.upload(uploadeItem);
        assertEquals("article2", result.getName());

        uploadeItem = UploadItem.builder()
                .content("page", new ByteArrayInputStream(data.getSimplePage()))
                .uri(SemanticUri.parse("items/page/simple-page"))
                .interval(new Interval())
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();

        result = service.upload(uploadeItem);
        assertEquals("simple-page", result.getName());
        assertEquals(1, result.getVersions().size());
        assertEquals(3, result.getVersions().stream().findFirst().get().getReferences().size());
        assertEquals(1, result.getHosts().size());
        assertEquals(host, result.getHosts().stream().findFirst().get());

    }

    @Test
    public void testOverwrite() throws Exception {
        UploadItem uploadeItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(SemanticUri.parse("items/article2"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        Item owerwrite = service.upload(uploadeItem);
        uploadeItem = UploadItem.builder()
                .content("page", new ByteArrayInputStream(data.getSimplePage()))
                .uri(SemanticUri.parse("items/page/simple-page"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        service.upload(uploadeItem);
        uploadeItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(SemanticUri.parse("items/article2"))
                .interval(new Interval(NOW.plusDays(1), NOW.plusDays(10)))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        service.overwrite(owerwrite.getUuid(), uploadeItem);

        try {
            userTransaction.begin();
            Item item = itemRepo.get(owerwrite.getUuid());
            assertEquals("article2", item.getName());
            assertEquals(3, item.getVersions().stream().filter(v -> v.isActive()).count());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testIncompleteUpload() throws Exception {
        InputStream is = new ByteArrayInputStream("test data".getBytes());
        UploadItem uploadeItem = UploadItem.builder()
                .content("TestRef", is)
                .uri(SemanticUri.parse("test/test/name"))
                .interval(new Interval())
                .build();

        catchException(service).upload(uploadeItem);
        assertTrue(caughtException().getCause() instanceof ConstraintViolationException);
        ConstraintViolationException ex = (ConstraintViolationException) caughtException().getCause();
        assertEquals(1, ex.getConstraintViolations().size());
        assertTrue(ex.getConstraintViolations().stream().findFirst().get().getMessage().startsWith("may not be null"));
    }

}
