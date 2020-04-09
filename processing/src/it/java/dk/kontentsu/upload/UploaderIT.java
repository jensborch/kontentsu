package dk.kontentsu.upload;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import dk.kontentsu.model.Host;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Version;
import dk.kontentsu.repository.HostRepository;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.test.ContentTestData;
import dk.kontentsu.test.TestEJBContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link dk.kontentsu.upload.Uploader}
 *
 * @author Jens Borch Christiansen
 */
public class UploaderIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private static EJBContainer container;
    private static ContentTestData data;
    private Host host;

    @Inject
    private HostRepository hostRepo;

    @Inject
    private ItemRepository itemRepo;

    @Inject
    private Uploader service;

    @Resource
    private UserTransaction userTransaction;

    @BeforeEachClass
    public static void setUpClass() {
        container = TestEJBContainer.create();
    }

    @AfterClass
    public static void tearDownClass() {
        if (container != null) {
            container.close();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        TestEJBContainer.inject(container, this);
        data = new ContentTestData();
        host = createHost("test_host");

    }

    private Host createHost(String name) throws Exception {
        try {
            userTransaction.begin();
            return hostRepo.findByName(name).orElseGet(() ->
                    hostRepo.save(new Host(name,
                            "Test description",
                            URI.create("ftp://myusername:mypassword@somehost/"),
                            "cdn/upload"))
            );
        } finally {
            userTransaction.commit();
        }
    }

    private Item getItem(final UUID id) throws Exception {
        try {
            userTransaction.begin();
            return itemRepo.get(id);
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            itemRepo.findAll().forEach(Item::delete);
        } finally {
            userTransaction.commit();
        }

    }

    @Test
    public void testUploadPlainText() throws Exception {
        Host textHost = createHost("text_host");
        InputStream is = new ByteArrayInputStream("test data".getBytes());
        UploadItem uploadItem = UploadItem.builder()
                .content("TestRef", is)
                .uri(new Item.URI("test/test-name"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("text", "plain"))
                .host("text_host")
                .encoding(StandardCharsets.UTF_8)
                .build();

        UUID id = service.upload(uploadItem);

        try {
            userTransaction.begin();
            Item r = itemRepo.get(id);
            assertEquals("name", r.getEdition().get());
            assertEquals(1, r.getVersions().size());
            assertEquals(1, ((Long) r.getVersions().stream().filter(v -> v.getInterval().getFrom().toInstant().equals(NOW.toInstant())).count()).intValue());
            assertEquals(1, ((Long) r.getVersions().stream().filter(v -> v.getInterval().getTo().toInstant().equals(Interval.INFINITE.toInstant())).count()).intValue());
            assertEquals(1, r.getHosts().size());
            assertEquals(textHost, r.getHosts().stream().findFirst().get());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testSimplePage() throws Exception {
        UploadItem uploadItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(new Item.URI("items/article2/"))
                .interval(new Interval())
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        Item result = getItem(service.upload(uploadItem));
        assertFalse(result.getEdition().isPresent());

        uploadItem = UploadItem.builder()
                .content("page", new ByteArrayInputStream(data.getSimplePage()))
                .uri(new Item.URI("items/page/simple-page/"))
                .interval(new Interval())
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();

        UUID id = service.upload(uploadItem);
        try {
            userTransaction.begin();
            Item r = itemRepo.get(id);
            assertFalse(r.getEdition().isPresent());
            assertEquals(1, r.getVersions().size());
            assertEquals(2, r.getVersions().stream().findFirst().get().getReferences().size());
            assertEquals(1, r.getHosts().size());
            assertEquals(host, r.getHosts().stream().findFirst().get());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testOverwrite() throws Exception {
        UploadItem uploadItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(new Item.URI("items/article2/"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        Item overwrite = getItem(service.upload(uploadItem));
        uploadItem = UploadItem.builder()
                .content("page", new ByteArrayInputStream(data.getSimplePage()))
                .uri(new Item.URI("items/page/simple-page/"))
                .interval(new Interval(NOW))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        service.upload(uploadItem);
        uploadItem = UploadItem.builder()
                .content("article2", new ByteArrayInputStream(data.getArticle(1)))
                .uri(new Item.URI("items/article2/"))
                .interval(new Interval(NOW.plusDays(1), NOW.plusDays(10)))
                .mimeType(new MimeType("application", "hal+json"))
                .encoding(StandardCharsets.UTF_8)
                .build();
        service.overwrite(overwrite.getUuid(), uploadItem);

        try {
            userTransaction.begin();
            Item item = itemRepo.get(overwrite.getUuid());
            assertFalse(item.getEdition().isPresent());
            assertEquals(3, item.getVersions().stream().filter(Version::isActive).count());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testOverwriteWithOverlap() throws Exception {
        UploadItem upload = UploadItem.builder()
                .uri(new Item.URI("items/test/"))
                .content("ref", new ByteArrayInputStream("{}".getBytes()))
                .encoding(StandardCharsets.UTF_8)
                .mimeType(MimeType.APPLICATION_JSON_TYPE)
                .interval(new Interval(NOW, NOW.plusDays(2)))
                .build();
        UUID id = service.uploadSync(upload);
        upload = UploadItem.builder()
                .uri(new Item.URI("items/test/"))
                .content("ref", new ByteArrayInputStream("{}".getBytes()))
                .encoding(StandardCharsets.UTF_8)
                .mimeType(MimeType.APPLICATION_JSON_TYPE)
                .interval(new Interval(NOW, NOW.plusDays(4)))
                .build();
        Set<UUID> versions = service.overwriteSync(id, upload);
        assertEquals(1, versions.size());
        UUID newid = versions.stream().findAny().get();
        assertNotEquals(id, newid);
        try {
            userTransaction.begin();
            assertEquals(new Interval(NOW, NOW.plusDays(4)), itemRepo.getVersion(newid).getInterval());
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void testIncompleteUpload() {
        InputStream is = new ByteArrayInputStream("test data".getBytes());
        UploadItem uploadItem = UploadItem.builder()
                .content("TestRef", is)
                .uri(new Item.URI("test/test-name"))
                .interval(new Interval())
                .build();

        catchException(service).upload(uploadItem);
        assertTrue(caughtException().getCause() instanceof ConstraintViolationException);
        ConstraintViolationException ex = (ConstraintViolationException) caughtException().getCause();
        assertEquals(1, ex.getConstraintViolations().size());
        assertTrue(ex.getConstraintViolations().stream().findFirst().get().getMessage().startsWith("may not be null"));
    }

}
