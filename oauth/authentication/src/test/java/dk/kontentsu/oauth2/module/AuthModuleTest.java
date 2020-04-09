package dk.kontentsu.oauth2.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link AuthModule}
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
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

    private JwtBuilder token;

    @BeforeEach
    public void setup() {
        module = new AuthModule();
        options = new Options().setSignatureKey("signature_key");
        clientSubject = new Subject();
        serviceSubject = new Subject();
        module.initialize(requestPolicy, responsePolicy, handler, options.asMap());
        when(messageInfo.getRequestMessage()).thenReturn(request);
        LocalDateTime now = LocalDateTime.now();
        token = Jwts.builder().setIssuer("Junit")
                .setSubject("test")
                .setIssuer("test")
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(now.plusHours(2).toInstant(ZoneOffset.UTC)))
                .claim("groups", new HashSet<>())
                .signWith(SignatureAlgorithm.HS512, "signature_key".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testNoToken() throws Exception {
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        verify(handler, times(0)).handle(any());
    }

    @Test
    public void testInvalidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        verify(handler, times(0)).handle(any());
    }

    @Test
    public void testUnsupportedCallbackException() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token.compact());
        doThrow(new UnsupportedCallbackException(null)).when(handler).handle(any());
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SEND_FAILURE, result);
    }

    @Test
    public void testValidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token.compact());
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        ArgumentCaptor<Callback[]> callbacks = ArgumentCaptor.forClass(Callback[].class);
        verify(handler).handle(callbacks.capture());
        assertEquals(2, callbacks.getAllValues().get(0).length);
        assertEquals("test", ((CallerPrincipalCallback) callbacks.getAllValues().get(0)[0]).getName());
    }

    @Test
    public void testOldToken() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String t = token.setIssuedAt(Date.from(now.minusHours(5).toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(now.minusHours(2).toInstant(ZoneOffset.UTC)))
                .compact();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + t);
        AuthStatus result = module.validateRequest(messageInfo, clientSubject, serviceSubject);
        assertEquals(AuthStatus.SUCCESS, result);
        verify(handler, times(0)).handle(any());
    }

}
