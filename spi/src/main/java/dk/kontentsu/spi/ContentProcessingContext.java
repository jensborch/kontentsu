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
public class ContentProcessingContext implements AlterableContext, Serializable, StartableContentContext {

    private static final long serialVersionUID = 2249914178328516867L;
    private final static int DEFAULT_SCOPE_NEXTING = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentProcessingContext.class);

    private final ThreadLocal<ArrayDeque<ContentState>> state = new ThreadLocal<>() {
        @Override
        protected ArrayDeque<ContentState> initialValue() {
            return new ArrayDeque<>(DEFAULT_SCOPE_NEXTING);
        }
    };

    private final transient ThreadLocal<ContentState> active = new ThreadLocal<>();

    public ContentProcessingContext() {
        LOGGER.info("Initlizing CDI content processing context...");
        ContentProcessingContextManager.getInstance().register(this);
    }

    @Override
    public void enter(ScopedContent content) {
        LOGGER.debug("Entering content processing scope...");
        ContentState newState = new ContentState(content);
        this.active.set(newState);
        this.state.get().push(newState);
    }

    @Override
    public void exit() {
        LOGGER.debug("Exiting content processing scope...");
        this.active.remove();
        this.state.get().pop();
    }

    @Override
    public ScopedContent getScopedContent() {
        return state.get().stream()
                .map(c -> c.content)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        Optional.ofNullable(active().get(contextual))
                .ifPresent(Instance::destroy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        requireActive();
        Map<Contextual<?>, Instance<?>> map = active();
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
        requireActive();
        return (T) Optional.ofNullable(active().get(contextual)).map(i -> i.bean).orElse(null);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ContentProcessingScoped.class;
    }

    private Map<Contextual<?>, Instance<?>> active() {
        return active.get().instances;
    }

    @Override
    public boolean isActive() {
        return active.get() != null;
    }

    private void requireActive() {
        if (!isActive()) {
            throw new ContextNotActiveException("Content processing scope is not active.");
        }
    }

    /**
     * Class wrapping a CDI bean instance.
     *
     * @param <T> type of bean to wrap
     */
    static class Instance<T> {

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

    private static class ContentState {

        final ScopedContent content;
        final Map<Contextual<?>, Instance<?>> instances;

        public ContentState(final ScopedContent content) {
            this.content = content;
            this.instances = new HashMap<>();
        }
    }

}
