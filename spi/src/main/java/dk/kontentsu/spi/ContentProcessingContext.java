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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context implementation for the {@link ContentProcessingScoped} scope
 * annotation.
 *
 * @author Jens Borch Christiansen
 */
public class ContentProcessingContext implements AlterableContext, Serializable {

    private static final long serialVersionUID = 2249914178328516867L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentProcessingContext.class);

    private static final ThreadLocal<ArrayDeque<Map<Contextual<?>, Instance<?>>>> INSTANCES = new ThreadLocal<>();
    private static final ThreadLocal<Parsable> CONTENT = new ThreadLocal<>();

    private static void initInstances() {
        if (INSTANCES.get() == null) {
            INSTANCES.set(new ArrayDeque<>());
        }
    }

    public static void execute(final ContentProcessingTask task, final Parsable content) {
        LOGGER.debug("Starting content CDI context");
        initInstances();
        Map<Contextual<?>, Instance<?>> map = new HashMap<>();
        INSTANCES.get().push(map);
        if (CONTENT.get() == null && content != null) {
            CONTENT.set(content);
        }
        try {
            task.run();
        } finally {
            map.values().stream().forEach(Instance::destroy);
            map.clear();
            INSTANCES.get().pop();
            if (INSTANCES.get().isEmpty()) {
                CONTENT.remove();
            }
            LOGGER.debug("Stopping content CDI context");
        }
    }

    public static void execute(final ContentProcessingTask task) {
        execute(task, null);
    }

    public static Parsable getContent() {
        return ContentProcessingContext.CONTENT.get();
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        Optional.ofNullable(INSTANCES.get().peek().get(contextual))
                .filter(Objects::nonNull)
                .ifPresent(Instance::destroy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException(ContentProcessingScoped.class.getName() + " is not active.");
        }
        Map<Contextual<?>, Instance<?>> map = INSTANCES.get().peek();
        if (map.containsKey(contextual)) {
            return (T) map.get(contextual);
        } else {
            Instance<T> i = new Instance<>(contextual, creationalContext);
            map.put(contextual, i);
            return i.create();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final Contextual<T> contextual) {
        if (!isActive()) {
            throw new ContextNotActiveException(ContentProcessingScoped.class.getName() + " is not active.");
        }
        return (T) Optional.ofNullable(INSTANCES.get().peek().get(contextual)).map(i -> i.bean).orElse(null);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ContentProcessingScoped.class;
    }

    @Override
    public boolean isActive() {
        return INSTANCES.get() != null && !INSTANCES.get().isEmpty();
    }

    /**
     * Class wrapping a CDI bean instance.
     *
     * @param <T> type of bean to wrap
     */
    private static class Instance<T> {

        private final CreationalContext<T> context;
        private final Contextual<T> contextual;

        private T bean;

        Instance(final Contextual<T> contextual, final CreationalContext<T> context) {
            this.contextual = contextual;
            this.context = context;
        }

        public T create() {
            if (bean == null) {
                this.bean = contextual.create(context);
            }
            return bean;
        }

        @SuppressWarnings("PMD.NullAssignment")
        public void destroy() {
            if (bean == null) {
                return;
            }
            contextual.destroy(bean, context);
            bean = null;
        }
    }

}
