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

import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps Java EE functionality for performing a login and obtaining
 * role information.
 *
 * @author Jens Borch Christiansen
 */
@RequestScoped
public class LoginProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenExposure.class);

    @Context
    HttpServletRequest request;

    public User login(final String username, final String password) {
        try {
            request.login(username, password);
            //Use JACC API to get groups from realm... might not work in all containers
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            Set<String> roles = subject.getPrincipals().stream()
                    .map(p -> p.getName())
                    //.filter(n -> request.isUserInRole(n))
                    .collect(Collectors.toSet());
            return new User(username, roles);
        } catch (PolicyContextException | ServletException ex) {
            String msg = "Login failed for user: " + username;
            LOGGER.info(msg);
            throw new NotAuthorizedException(msg, ex);
        }
    }

}
