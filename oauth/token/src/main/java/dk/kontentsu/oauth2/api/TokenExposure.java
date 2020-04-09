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
package dk.kontentsu.oauth2.api;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.kontentsu.oauth2.api.configuration.Config;
import dk.kontentsu.oauth2.api.model.TokenRepresentation;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Get OAuth2 access token - i.e. a JWT token.
 *
 * @author Jens Borch Christiansen
 */
@Path("/token")
@Stateless
public class TokenExposure {

    @Inject
    private Config config;

    @Inject
    private LoginProvider loginProvider;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(@FormParam("grant_type") @NotNull final String grantType,
            @FormParam("username") @NotNull final String username,
            @FormParam("password") @NotNull final String password) {
        if (GrantType.isSupported(grantType)) {
            User user = loginProvider.login(username, password);
            LocalDateTime now = LocalDateTime.now();
            String token = Jwts.builder().setIssuer("Kontentsu")
                    .setSubject(username)
                    .setIssuer(config.issuer())
                    .setIssuedAt(toDate(now))
                    .setExpiration(getExpirationDate(now))
                    .claim("groups", user.getRoles())
                    .signWith(SignatureAlgorithm.HS512, config.signatureKey().getBytes(StandardCharsets.UTF_8))
                    .compact();
            return Response.ok(new TokenRepresentation(token, config)).build();
        } else {
            throw new NotAuthorizedException("Unknown grant type: " + grantType);
        }
    }

    private Date getExpirationDate(final LocalDateTime now) {
        return toDate(now.plusMinutes(config.timeout()));
    }

    private static Date toDate(final LocalDateTime date) {
        return Date.from(date.toInstant(ZoneOffset.UTC));
    }
}
