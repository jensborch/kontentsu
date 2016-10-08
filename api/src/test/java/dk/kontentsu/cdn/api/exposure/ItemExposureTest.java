package dk.kontentsu.cdn.api.exposure;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;

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
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.repository.ItemRepository;
import dk.kontentsu.cdn.upload.UploadService;

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
        List<Item> items = new ArrayList<>();
        Item i = new Item(new SemanticUri(new SemanticUriPath("item", "test"), "test-xl.jpg"));
        items.add(i);
        Mockito.when(itemRepo.find(Mockito.any(Item.Criteria.class))).thenReturn(items);
    }

    @Test
    public void testFind() throws Exception {
        given().get(target("items").getUri())
                .then()
                .statusCode(200)
                .body("[0].uri", is("item/test/test-xl.jpg"));
    }

}
