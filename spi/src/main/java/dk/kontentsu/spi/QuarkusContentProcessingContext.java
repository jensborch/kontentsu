package dk.kontentsu.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import io.quarkus.arc.ContextInstanceHandle;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.impl.ContextInstanceHandleImpl;

/**
 * Implementation of the CDI content processing context in Quarkus.
 */
public class QuarkusContentProcessingContext implements InjectableContext, StartableContentContext {

    private final static int DEFAULT_SCOPE_NEXTING = 10;

    private final ThreadLocal<ArrayDeque<ContentState>> state = new ThreadLocal<>() {
        @Override
        protected ArrayDeque<ContentState> initialValue() {
            return new ArrayDeque<>(DEFAULT_SCOPE_NEXTING);
        }
    };

    private final ThreadLocal<ContentState> active = new ThreadLocal<>();

    @Override
    public void enter(final ScopedContent content) {
        Objects.requireNonNull(content);
        ContentState newState = new ContentState(content);
        this.active.set(newState);
        this.state.get().push(newState);
    }

    @Override
    public void exit() {
        this.active.remove();
        this.state.get().pop();
    }

    @Override
    public ScopedContent getScopedContent() {
        return active.get().content;
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        active().destroy();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ContentProcessingScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Objects.requireNonNull(contextual, "Contextual must not be null.");
        requireActive();
        ContextInstanceHandle<T> instanceHandle = active().get(contextual);
        if (instanceHandle != null) {
            return instanceHandle.get();
        } else if (creationalContext != null) {
            T createdInstance = contextual.create(creationalContext);
            instanceHandle = new ContextInstanceHandleImpl<>((InjectableBean<T>) contextual, createdInstance,
                    creationalContext);
            active().put(contextual, instanceHandle);
            return createdInstance;
        } else {
            return null;
        }
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return active.get() != null;
    }

    @Override
    public void destroy() {
        if (isActive()) {
            active().destroy();
        }
        exit();
    }

    @Override
    public ContextState getState() {
        requireActive();
        return active();
    }

    private ContentProcessingContextState active() {
        return active.get().state;
    }

    private void requireActive() {
        if (!isActive()) {
            throw new ContextNotActiveException("Content processing scope is not active.");
        }
    }

    /**
     * Container for available beans in the content processing context.
     */
    private static class ContentProcessingContextState extends ContentProcessingScope implements ContextState {

        private final ConcurrentMap<Contextual<?>, ContextInstanceHandle<?>> beanToInstanceHandleMap = new ConcurrentHashMap<>();

        <T> void put(Contextual<T> bean, ContextInstanceHandle<T> handle) {
            beanToInstanceHandleMap.put(bean, handle);
        }

        <T> void remove(Contextual<T> bean) {
            beanToInstanceHandleMap.remove(bean);
        }

        @SuppressWarnings("unchecked")
        <T> ContextInstanceHandle<T> get(Contextual<T> bean) {
            return (ContextInstanceHandle<T>) beanToInstanceHandleMap.get(bean);
        }

        void destroy() {
            beanToInstanceHandleMap.values().forEach(ContextInstanceHandle::destroy);
            beanToInstanceHandleMap.clear();
        }

        @Override
        public Map<InjectableBean<?>, Object> getContextualInstances() {
            return beanToInstanceHandleMap.values().stream()
                    .collect(Collectors.toMap(ContextInstanceHandle::getBean, ContextInstanceHandle::get));
        }
    }

    private static class ContentState {

        final ScopedContent content;
        final ContentProcessingContextState state;

        public ContentState(final ScopedContent content) {
            this.content = content;
            this.state = new ContentProcessingContextState();
        }

    }
}
