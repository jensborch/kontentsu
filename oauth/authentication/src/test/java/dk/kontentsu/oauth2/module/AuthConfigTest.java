package dk.kontentsu.oauth2.module;


import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link AuthConfig}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthConfigTest {

    @Mock
    private CallbackHandler handler;

    private AuthConfig config;
    private Options options;

    @Before
    public void setup() {
        options = new Options();
        config = new AuthConfig("appContext", "messageLayer", handler, options);
    }

    @Test
    public void testGetAuthContext() throws Exception {
        Subject serviceSubject = new Subject();
        AuthModule module = config.getAuthContext("authContextID", serviceSubject, new HashMap());
        assertNotNull(module);
    }

}
