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

import javax.ws.rs.GET;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Register and configure cache filter for all methods with a cache annotation.
 *
 * @author Jens Borch Christiansen
 */
@Provider
public class CacheFeature implements DynamicFeature {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(GET.class) != null) {
            Cache cc = resourceInfo.getResourceMethod().getAnnotation(Cache.class);
            NoCache nc = resourceInfo.getResourceMethod().getAnnotation(NoCache.class);
            if (nc != null) {
                LOGGER.debug("Found NoCache annotation on method {}", resourceInfo.getResourceMethod());
                context.register(new CacheFilter(nc));
            } else if (cc != null) {
                LOGGER.debug("Found Cache annotation on method {}", resourceInfo.getResourceMethod());
                context.register(new CacheFilter(cc));
            }
        } else {
            LOGGER.warn("Found Cache annotation on non GET method {}", resourceInfo.getResourceMethod());
        }
    }

}
