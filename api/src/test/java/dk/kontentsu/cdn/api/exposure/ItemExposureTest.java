package dk.kontentsu.cdn.api.exposure;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dk.kontentsu.cdn.api.ObjectMapperProvider;
import dk.kontentsu.cdn.api.configuration.Config;
import dk.kontentsu.cdn.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.PersistenceExceptionMapper;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.internal.Item;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.upload.UploadService;

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
    private UploadService service;

    private List<Item> items;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(ItemExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(PersistenceExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(ObjectMapperProvider.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(itemRepo).to(ItemRepository.class);
                        bind(config).to(Config.class);
                        bind(service).to(UploadService.class);
                    }
                });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        items = new ArrayList<>();
        Item i = new Item(new SemanticUri(new SemanticUriPath("item", "test"), "test-xl.jpg"));
        items.add(i);
        Mockito.when(itemRepo.find(Mockito.any(Item.Criteria.class))).thenReturn(items);
        Mockito.when(itemRepo.get(items.get(0).getUuid())).thenReturn(i);
    }

    @Test
    public void testList() throws Exception {
        given().get(target("items").getUri())
                .then()
                .statusCode(200)
                .body("[0].uri", is("item/test/test-xl.jpg"));
    }

    @Test
    public void testGet() throws Exception {
        given().get(target("items").path(items.get(0).getUuid().toString()).getUri())
                .then()
                .statusCode(200)
                .body("uri", is("item/test/test-xl.jpg"));
    }

    @Test
    public void testGetNoResults() throws Exception {
        UUID uuid = UUID.randomUUID();
        Mockito.when(itemRepo.get(uuid)).thenThrow(new NoResultException("Test"));
        given().get(target("items").path(uuid.toString()).getUri())
                .then()
                .statusCode(404);
    }

}
