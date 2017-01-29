package dk.kontentsu.oauth2.module;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;

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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

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
        options = new Options().setSignatureKey("signature_key");
        clientSubject = new Subject();
        serviceSubject = new Subject();
        module.initialize(requestPolicy, responsePolicy, handler, options.asMap());
        when(messageInfo.getRequestMessage()).thenReturn(request);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearere token");
    }

    @Test
    public void testInvalidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        verify(handler, times(0)).handle(any());
    }

    @Test
    public void testValidToken() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String token = Jwts.builder().setIssuer("Junit")
                .setSubject("test")
                .setIssuer("test")
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(now.plusHours(2).toInstant(ZoneOffset.UTC)))
                .claim("groups", new HashSet<>())
                .signWith(SignatureAlgorithm.HS512, "signature_key".getBytes(StandardCharsets.UTF_8))
                .compact();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        verify(handler).handle(any());
    }

}
