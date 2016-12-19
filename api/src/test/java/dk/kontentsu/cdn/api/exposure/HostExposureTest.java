package dk.kontentsu.cdn.api.exposure;

import dk.kontentsu.cdn.api.exposure.HostExposure;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;

import dk.kontentsu.cdn.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.PersistenceExceptionMapper;
import dk.kontentsu.model.internal.Host;
import dk.kontentsu.repository.HostRepository;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
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
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(HostExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(PersistenceExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(hostRepo).to(HostRepository.class);
                    }
                });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<Host> hosts = new ArrayList<>();
        Host h = new Host("name", "description", new URI("http://test"), "test/test");
        hosts.add(h);
        when(hostRepo.findAll()).thenReturn(hosts);
    }

    @Test
    public void testFind() throws Exception {
        given().get(target("hosts").getUri())
                .then()
                .statusCode(200)
                .body("[0].rel", is("host"));
    }

}
