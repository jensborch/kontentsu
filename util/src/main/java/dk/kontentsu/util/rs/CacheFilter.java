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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * Filter for setting cache headers based on cache annotations.
 *
 * @author Jens Borch Christiansen
 */
public class CacheFilter implements ContainerResponseFilter {

    private final List<String> cacheControl;

    CacheFilter(final NoCache nc) {
        this.cacheControl = new ArrayList<>();
        if (nc != null) {
            this.cacheControl.add("no-cache");
        }
    }

    CacheFilter(final Cache cc) {
        this.cacheControl = new ArrayList<>();
        if (cc != null) {
            if (cc.isPrivate()) {
                this.cacheControl.add("private");
            }
            if (cc.mustRevalidate()) {
                this.cacheControl.add("must-revalidate");
            }
            if (cc.noStore()) {
                this.cacheControl.add("no-store");
            }
            if (cc.noTransform()) {
                this.cacheControl.add("no-transform");
            }
            if (cc.proxyRevalidate()) {
                this.cacheControl.add("proxy-revalidate");
            }
            if (cc.maxAge() >= 0) {
                this.cacheControl.add("max-age=" + cc.unit().toSeconds(cc.maxAge()));
            }
            if (cc.sMaxAge() >= 0) {
                this.cacheControl.add("s-max-age=" + cc.unit().toSeconds(cc.sMaxAge()));
            }
        }
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        if (!cacheControl.isEmpty()) {
            response.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, cacheControl
                    .stream()
                    .collect(Collectors.joining(", ")));
        }
    }

}
