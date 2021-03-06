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
package dk.kontentsu.model.processing;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import dk.kontentsu.model.Content;
import dk.kontentsu.spi.ContentProcessingScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Producer for injection content into a CDI bean that is annotated with {@link ContentProcessingScoped}.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationScoped
public class ContentProducer {

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        LOGGER.info("Loading CDI content producer...");
    }

    @Produces
    @ContentProcessingScoped
    public Content getContent() {
        LOGGER.debug("Injecting content into class");
        return InjectableContentProcessingScope.retrieve();
    }

}
