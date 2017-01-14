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

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
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

    private static final String JAVAX_SUBJECT_CONTAINER = "javax.security.auth.Subject.container";
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenExposure.class);

    @Inject
    private HttpServletRequest request;

    /**
     * Perform login using
     * {@link HttpServletRequest#login(java.lang.String, java.lang.String)} and
     * retrieve roles for authenticated user.
     *
     * @param username name of user
     * @param password for user
     * @return a {@link Principal} containing user roles.
     */
    public User login(final String username, final String password) {
        try {
            request.login(username, password);
            return new User(username, getRoles());
        } catch (PolicyContextException | ServletException ex) {
            String msg = "Login failed for user: " + username;
            LOGGER.info(msg);
            throw new NotAuthorizedException(msg, ex);
        }
    }

    /**
     * Use JACC API to get <code>Subject</code> for current authenticated user
     * from realm.
     *
     * @return the <code>Subject</code> for current user
     * @throws PolicyContextException if an error occurs
     */
    public Subject getSubject() throws PolicyContextException {
        return (Subject) PolicyContext.getContext(JAVAX_SUBJECT_CONTAINER);
    }

    /**
     * Use JACC API to get list for roles for current authenticated user from
     * realm.
     *
     * @return list of roles
     * @throws PolicyContextException if an error occurs
     */
    public Set<String> getRoles() throws PolicyContextException {
        return getSubject().getPrincipals().stream()
                .map(p -> p.getName())
                .collect(Collectors.toSet());
    }

}
