
package dk.kontentsu.oauth2.module;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import javax.security.auth.message.config.AuthConfigFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link AuthConfigProvider}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthConfigProviderTest {

    @Mock
    private AuthConfigFactory factory;

    @Test
    @SuppressWarnings("unchecked")
    public void testSelfRegister() {
        AuthConfigProvider provider = new AuthConfigProvider(new HashMap(), factory);
        verify(factory).registerConfigProvider(provider, "HttpServlet", null, "OAuth2 SAM authentication config provider");
    }

}
