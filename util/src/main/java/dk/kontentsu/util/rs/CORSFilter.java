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
package dk.kontentsu.util.rs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Very basic CORS filter.
 *
 * @author Jens Borch Christiansen
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        response.getHeaders().add(ALLOW_ORIGIN, "*");
        response.getHeaders().add(ALLOW_HEADERS, "origin, content-type, accept, authorization");
        response.getHeaders().add(ALLOW_CREDENTIALS, "true");
        response.getHeaders().add(ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }

}
