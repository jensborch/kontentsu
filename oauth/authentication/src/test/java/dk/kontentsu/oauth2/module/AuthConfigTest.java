package dk.kontentsu.oauth2.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessageInfo;
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

    @Mock
    private MessageInfo messageInfo;

    private AuthConfig config;

    @Before
    public void setup() {
        config = new AuthConfig("appContext", "messageLayer", handler, new Options());
    }

    @Test
    public void testGetAuthContext() throws Exception {
        Subject serviceSubject = new Subject();
        AuthModule module = config.getAuthContext("authContextID", serviceSubject, new HashMap());
        assertNotNull(module);
    }

    @Test
    public void testGetAuthContextID() {
        when(messageInfo.getMap()).thenReturn(new HashMap());
        String result = config.getAuthContextID(messageInfo);
        assertNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetAuthContextIDNotMandatory() {
        Map map = new HashMap();
        map.put("javax.security.auth.message.MessagsePolicy.isMandatory", "wrong");
        when(messageInfo.getMap()).thenReturn(map);
        String result = config.getAuthContextID(messageInfo);
        assertNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetAuthContextIDMandatory() {
        Map map = new HashMap();
        map.put("javax.security.auth.message.MessagsePolicy.isMandatory", "true");
        when(messageInfo.getMap()).thenReturn(map);
        when(messageInfo.toString()).thenReturn("junit");
        String result = config.getAuthContextID(messageInfo);
        assertEquals("junit", result);
    }

}
