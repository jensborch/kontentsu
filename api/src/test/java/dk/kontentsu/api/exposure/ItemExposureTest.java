package dk.kontentsu.api.exposure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Application;

import dk.kontentsu.api.ObjectMapperProvider;
import dk.kontentsu.api.configuration.Config;
import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.upload.Uploader;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link ItemExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class ItemExposureTest extends JerseyTest {

    @Mock
    private ItemRepository itemRepo;

    @Mock
    private Config config;

    @Mock
    private Uploader service;

    private List<Item> items;

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(ItemExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(ObjectMapperProvider.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(itemRepo).to(ItemRepository.class);
                        bind(config).to(Config.class);
                        bind(service).to(Uploader.class);
                    }
                });
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        items = new ArrayList<>();
        Item i = new Item(new Term().append("item").append("test"), "xl", new MimeType("image", "jpg"));
        items.add(i);
        Mockito.when(itemRepo.find(Mockito.any(Item.Criteria.class))).thenReturn(items);
        Mockito.when(itemRepo.get(items.get(0).getUuid())).thenReturn(i);
    }

    @Test
    public void testList() {
        given().get(target("items").getUri())
                .then()
                .statusCode(200)
                .body("[0].uri", is("/item/test/test-xl.jpg"));
    }

    @Test
    public void testGet() {
        given().get(target("items").path(items.get(0).getUuid().toString()).getUri())
                .then()
                .statusCode(200)
                .body("uri", is("/item/test/test-xl.jpg"));
    }

    @Test
    public void testGetNoResults() {
        UUID uuid = UUID.randomUUID();
        Mockito.when(itemRepo.get(uuid)).thenThrow(new EJBException(new NoResultException("Test")));
        given().get(target("items").path(uuid.toString()).getUri())
                .then()
                .statusCode(404);
    }

}
