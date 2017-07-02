package dk.kontentsu.oauth2.api;

import dk.kontentsu.oauth2.api.configuration.Config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import dk.kontentsu.oauth2.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.oauth2.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.oauth2.api.exceptionmappers.NotAuthorizedExceptionMapper;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link TokenExposure}
 */
public class TokenExposureTest extends JerseyTest {

    @Mock
    private LoginProvider login;

    @Mock
    private Config config;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(TokenExposure.class)
                .register(JacksonFeature.class)
                .register(NotAuthorizedExceptionMapper.class)
                .register(ContainerExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(login).to(LoginProvider.class);
                        bind(config).to(Config.class);
                    }
                });
    }

    @Before
    public void setup() throws Exception {
        Collection<String> roles = new ArrayList<>();
        roles.add("test");
        when(config.issuer()).thenReturn("junit");
        when(config.timeout()).thenReturn(10);
        when(config.signatureKey()).thenReturn("junit");
        when(login.login("user", "password")).thenReturn(new User("uesr", roles));
        when(login.login("unknown", "password")).thenThrow(new NotAuthorizedException("junit", new Exception()));
    }

    @Test
    public void test() {
        given().contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "password")
                .formParam("username", "user")
                .formParam("password", "password")
                .post(target("token").getUri())
                .then()
                .statusCode(200)
                .body("token_type", is("BearerToken"));
    }

    @Test
    public void testUnknown() {
        given().contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "password")
                .formParam("username", "unknown")
                .formParam("password", "password")
                .post(target("token").getUri())
                .then()
                .statusCode(401)
                .body("error", is("unauthorized_client"));
    }

    @Test
    public void testUnsupportedGrantType() {
        given().contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "junit")
                .formParam("username", "user")
                .formParam("password", "password")
                .post(target("token").getUri())
                .then()
                .statusCode(401)
                .body("error", is("unauthorized_client"));
    }

    @Test
    public void testInvalidRequest() {
        given().contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "password")
                .formParam("password", "password")
                .post(target("token").getUri())
                .then()
                .statusCode(400)
                .body("error", is("invalid_request"));
    }

}
