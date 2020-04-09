
package dk.kontentsu.oauth2.module;

import static org.mockito.Mockito.verify;

import java.util.HashMap;

import javax.security.auth.message.config.AuthConfigFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link AuthConfigProvider}.
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
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
