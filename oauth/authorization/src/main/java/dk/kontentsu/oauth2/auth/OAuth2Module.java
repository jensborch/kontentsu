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
package dk.kontentsu.oauth2.auth;

import dk.kontentsu.oauth2.Config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAauth2 Java EE server authentication module.
 *
 * @author Jens Borch Christiansen
 */
public class OAuth2Module implements ServerAuthModule, ServerAuthContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Module.class);
    private final Config config = ConfigCache.getOrCreate(Config.class);
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
        //AuthOptions authOptions = new AuthOptions(options);
        //options.get("javax.security.auth.login.LoginContext");
        this.handler = handler;
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
                        .setSigningKey(config.signatureKey().getBytes())
                        .parseClaimsJws(token.get());

                handler.handle(new Callback[]{
                    new PasswordCallback("prompt", false),
                    new CallerPrincipalCallback(clientSubject, claims.getBody().getSubject()),
                    new GroupPrincipalCallback(clientSubject, getRoles(claims))});

            } catch (JwtException e) {
                LOGGER.warn("Error in JWT token retrived from authorization header", e);
            } catch (UnsupportedCallbackException | IOException e) {
                LOGGER.error("Error... ", e);
            }
        }
        return AuthStatus.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private static String[] getRoles(final Jws<Claims> claims) {
        try {
            return (String[]) (claims.getBody().get("groups", Collection.class)).toArray();
        } catch (ClassCastException ex) {
            return new String[0];
        }
    }

    private Optional<String> getBearerToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authHeader.substring("Bearer".length()).trim());
    }

    public static void register(final ServletContext context) {
        AuthConfigFactory.getFactory().registerConfigProvider(
                new OAuth2ConfigProvider(),
                "HttpServlet",
                getAppContextID(context),
                "OAuth2 SAM authentication config provider"
        );
    }

    public static String getAppContextID(final ServletContext context) {
        return context.getVirtualServerName() + " " + context.getContextPath();
    }

}
