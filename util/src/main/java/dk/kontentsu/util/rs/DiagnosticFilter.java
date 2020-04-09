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

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

/**
 * Simple diagnostic filter that add a log token to log4j's Mapped Diagnostic
 * Context (MDC).
 *
 * @author Jens Borch Christiansen
 */
@Provider
public class DiagnosticFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER_LOG_TOKEN = "X-Log-Token";
    private static final String MDC_LOG_TOKEN = "token";

    @Context
    private HttpHeaders headers;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        MDC.put(MDC_LOG_TOKEN, Optional
                .ofNullable(headers.getHeaderString(HEADER_LOG_TOKEN))
                .orElse(UUID.randomUUID().toString()));
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        MDC.clear();
    }
}
