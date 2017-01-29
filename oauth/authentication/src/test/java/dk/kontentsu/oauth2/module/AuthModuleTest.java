package dk.kontentsu.oauth2.module;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link AuthModule}
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthModuleTest {

    @Mock
    private MessagePolicy requestPolicy;

    @Mock
    private MessagePolicy responsePolicy;

    @Mock
    private CallbackHandler handler;

    @Mock
    private MessageInfo messageInfo;

    @Mock
    private HttpServletRequest request;

    private Subject clientSubject;

    private Subject serviceSubject;

    private Options options;

    private AuthModule module;

    @Before
    public void setup() throws Exception {
        module = new AuthModule();
        options = new Options();
        clientSubject = new Subject();
        serviceSubject = new Subject();
        module.initialize(requestPolicy, responsePolicy, handler, options.asMap());
        when(messageInfo.getRequestMessage()).thenReturn(request);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearere token");
    }

    @Test
    public void testInvalidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearere token");
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        assertEquals(0, clientSubject.getPrincipals().size());
    }

}
