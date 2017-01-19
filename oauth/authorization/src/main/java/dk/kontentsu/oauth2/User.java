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
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User principal with roles.
 *
 * @author Jens Borch Christiansen
 */
public class User implements Principal {

    private String name;
    private Set<String> roles;

    public User(final String name, final Collection<String> roles) {
        this.name = name;
        this.roles = new HashSet<>();
        this.roles.addAll(roles);
    }

    public User(final Jws<Claims> claims) {
        this(claims.getBody().getSubject(), getRoles(claims));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getRoles(final Jws<Claims> claims) {
        try {
            return (Collection<String>) claims.getBody().get("groups", Collection.class);
        } catch (ClassCastException ex) {
            return new HashSet<>();
        }
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public String getName() {
        return name;
    }

}
