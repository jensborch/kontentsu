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
package dk.kontentsu.cdn.spi;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI extension for {@link ContentProcessingScoped}.
 *
 * @author Jens Borch Christiansen
 */
public class ContentExtension implements Extension, Serializable {

    private static final long serialVersionUID = -2791994163919507379L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtension.class);

    static {
        LOGGER.info("Loading CDI content extension...");
    }

    public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
        LOGGER.info("Adding content CDI scope...");
        event.addScope(ContentProcessingScoped.class, true, false);
    }

    public void afterBeanDiscovery(@Observes final AfterBeanDiscovery event) {
        LOGGER.info("Adding content CDI context...");
        event.addContext(new ContentContext());
    }
}
