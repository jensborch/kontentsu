package dk.kontentsu.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.test.TestEJBContainer;

/**
 * Test for {@link UploadService}.
 */
public class UploadServiceIT {

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static EJBContainer container;

    @Inject
    private UploadService service;

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
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOverwrite() throws Exception {
        UploadItem upload = UploadItem.builder()
                .uri(SemanticUri.parse("items/test"))
                .content("ref", new ByteArrayInputStream("{}".getBytes()))
                .encoding(StandardCharsets.UTF_8)
                .mimeType(MimeType.APPLICATION_JSON_TYPE)
                .interval(new Interval(NOW, NOW.plusDays(2)))
                .build();
        UUID id = service.uploadSync(upload);
        upload = UploadItem.builder()
                .uri(SemanticUri.parse("items/test"))
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

}
