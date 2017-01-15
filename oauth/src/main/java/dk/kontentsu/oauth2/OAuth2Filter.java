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
package dk.kontentsu.oauth2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for retrieving and validating OAuth2 bearer token and setting security
 * context.
 *
 * @author Jens Borch Christiansen
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class OAuth2Filter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Filter.class);
    private final Config config = ConfigCache.getOrCreate(Config.class);

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        Optional<String> token = getBearerToken(context.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (token.isPresent()) {
            try {
                Jws<Claims> claims = Jwts.parser()
                        .setSigningKey(config.signatureKey().getBytes())
                        .parseClaimsJws(token.get());

                String scheme = context.getUriInfo().getRequestUri().getScheme();
                context.setSecurityContext(new OAuth2SecurityContext(new User(claims), scheme));
            } catch (JwtException e) {
                LOGGER.warn("Error in JWT token retrived from authorization header", e);
            }
        }
    }

    private Optional<String> getBearerToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authHeader.substring("Bearer".length()).trim());
    }

}
