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
package dk.kontentsu.oauth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Get OAuth2 access token - i.e. a JWT token.
 *
 * @author Jens Borch Christiansen
 */
@Path("token")
@Stateless
public class TokenExposure {

    private static final int TIMEOUT = 30;

    @Resource SessionContext ctx;

    @Context
    private HttpServletRequest request;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response getToken(@FormParam("grant_type") final String grantType,
            @FormParam("username") final String username,
            @FormParam("password") final String password,
            @HeaderParam("Authorization") final String authorization) {
        try {
            request.login(username, password);
            //Use JACC API to get groups from realm... might not work in all containers
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            Set<String> groups = subject.getPrincipals().stream()
                    .map(p -> p.getName())
                    //.filter(n -> request.isUserInRole(n))
                    .collect(Collectors.toSet());
            byte[] key = getSignatureKey();
            Date expirationDate = Date.from(LocalDateTime.now().plusMinutes(TIMEOUT).toInstant(ZoneOffset.UTC));
            String jwt = Jwts.builder().setIssuer("Kontentsu")
                    .setSubject(username)
                    .setExpiration(expirationDate)
                    .claim("groups", groups)
                    .signWith(SignatureAlgorithm.HS256, key)
                    .compact();
            return Response.ok(new TokenRepresentation(jwt)).build();
        } catch (ServletException | PolicyContextException ex) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    private byte[] getSignatureKey() {
        return new byte[0];
    }

}
