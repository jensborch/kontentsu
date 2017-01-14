package dk.kontentsu.oauth2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private TokenExposure exposure;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(TokenExposure.class)
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
        when(login.login("unknown", "password")).thenThrow(new LoginException("junit", new Exception()));
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
                .statusCode(401);
    }

}
