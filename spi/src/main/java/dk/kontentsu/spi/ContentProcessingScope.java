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
package dk.kontentsu.spi;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.spi.Contextual;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Class for managing the content processing scope defined using the
 * {@link ContentProcessingScoped} annotation.
 *
 * @author Jens Borch Christiansen
 */
public class ContentProcessingScope implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadLocal<ArrayDeque<Map<Contextual<?>, ContentProcessingContext.Instance<?>>>> INSTANCES = new ThreadLocal<>();

    private Map<Contextual<?>, ContentProcessingContext.Instance<?>> map;

    public ContentProcessingScope() {
        if (INSTANCES.get() == null) {
            INSTANCES.set(new ArrayDeque<>());
        }
    }

    Map<Contextual<?>, ContentProcessingContext.Instance<?>> pop() {
        return INSTANCES.get().pop();
    }

    Map<Contextual<?>, ContentProcessingContext.Instance<?>> peek() {
        return INSTANCES.get().peek();
    }

    void push(final Map<Contextual<?>, ContentProcessingContext.Instance<?>> map) {
        INSTANCES.get().push(map);
    }

    boolean isActive() {
        return INSTANCES.get() != null && !INSTANCES.get().isEmpty();
    }

    public void start() {
        LOGGER.debug("Starting CDI scope");
        map = new HashMap<>();
        INSTANCES.get().push(map);
    }

    @Override
    public void close() {
        if (map != null) {
            map.values().stream().forEach(ContentProcessingContext.Instance::destroy);
            map.clear();
            INSTANCES.get().pop();
            LOGGER.debug("Stopping CDI scope");
        } else {
            LOGGER.warn("CDI scope has not been startet");
        }
    }

    public boolean isRootScope() {
        return INSTANCES.get() != null && INSTANCES.get().isEmpty();
    }
}
