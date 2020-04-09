package dk.kontentsu.api.exposure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;

import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.model.Host;
import dk.kontentsu.repository.HostRepository;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link HostExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class HostExposureTest extends JerseyTest {

    @Mock
    private HostRepository hostRepo;

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(HostExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(hostRepo).to(HostRepository.class);
                    }
                });
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<Host> hosts = new ArrayList<>();
        Host h = new Host("name", "description", new URI("http://test"), "test/test");
        hosts.add(h);
        when(hostRepo.findAll()).thenReturn(hosts);
    }

    @Test
    public void testFind() {
        given().get(target("hosts").getUri())
                .then()
                .statusCode(200)
                .body("[0].rel", is("host"));
    }

}
