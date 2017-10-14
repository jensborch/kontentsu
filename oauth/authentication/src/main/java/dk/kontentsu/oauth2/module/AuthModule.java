/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.oauth2.module;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.ClientAuthContext;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ClientAuthModule;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OAuth2 JASPIC server authentication module.
 *
 * @author Jens Borch Christiansen
 */
public class AuthModule implements ServerAuthModule, ServerAuthContext, ClientAuthContext, ClientAuthModule {

    private static final Logger LOGGER = LogManager.getLogger();
    private Options config;
    private CallbackHandler handler;

    @Override
    public Class[] getSupportedMessageTypes() {
        return new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class};
    }

    @Override
    public void initialize(final MessagePolicy requestPolicy,
            final MessagePolicy responsePolicy,
            final CallbackHandler handler,
            @SuppressWarnings("rawtypes") final Map options) throws AuthException {
        this.handler = handler;
        this.config = new Options(options);
    }

    @Override
    public void cleanSubject(final MessageInfo info, final Subject subject) throws AuthException {
        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        subject.getPrivateCredentials().clear();
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo,
            final Subject clientSubject,
            final Subject serviceSubject) throws AuthException {
        final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        Optional<String> token = getBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token.isPresent()) {
            try {
                Jws<Claims> claims = Jwts.parser()
                        .setSigningKey(config.getSignatureKey())
                        .parseClaimsJws(token.get());

                String username = claims.getBody().getSubject();
                handler.handle(new Callback[]{
                    new CallerPrincipalCallback(clientSubject, username),
                    new GroupPrincipalCallback(clientSubject, getRoles(claims))});

                LOGGER.debug("Subject after invoking callbacks: {}", clientSubject);
                LOGGER.debug("Principal: " + username);
            } catch (JwtException e) {
                LOGGER.warn("Error in JWT token retrieved from authorization header", e);
            } catch (UnsupportedCallbackException | IOException e) {
                LOGGER.error("Error in server authentication module", e);
                return AuthStatus.SEND_FAILURE;
            }
        }
        return AuthStatus.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private static String[] getRoles(final Jws<Claims> claims) {
        List<String> groups = (List<String>) claims.getBody().get("groups", Collection.class)
                .stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());
        return groups.toArray(new String[groups.size()]);
    }

    private Optional<String> getBearerToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authHeader.substring("Bearer".length()).trim());
    }

    @Override
    public AuthStatus secureRequest(final MessageInfo messageInfo, final Subject clientSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    @Override
    public AuthStatus validateResponse(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

}
