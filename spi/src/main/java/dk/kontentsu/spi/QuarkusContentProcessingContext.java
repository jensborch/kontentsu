package dk.kontentsu.spi;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class QuarkusContentProcessingContext implements InjectableContext {

    private static final ThreadLocal<ContentProcessingContextState> STATE = new ThreadLocal<>();

    @Override
    public void destroy(Contextual<?> contextual) {
        Optional.ofNullable(STATE.get()).ifPresent(ContentProcessingContextState::destroy);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ContentProcessingScoped.class;
    }

    private ContentProcessingContextState initContextState() {
        ContentProcessingContextState s = new ContentProcessingContextState();
        STATE.set(s);
        return s;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Objects.requireNonNull(contextual, "Contextual must not be null");
        if (!isActive()) {
            throw new ContextNotActiveException(ContentProcessingScoped.class.getName() + " is not active.");
        }
        ContentProcessingContextState state = Optional.ofNullable(STATE.get()).orElseGet(this::initContextState);
        ContextInstanceHandle<T> instanceHandle = state.get(contextual);
        if (instanceHandle != null) {
            return instanceHandle.get();
        } else if (creationalContext != null) {
            T createdInstance = contextual.create(creationalContext);
            instanceHandle = new ContextInstanceHandleImpl<>((InjectableBean<T>) contextual, createdInstance,
                    creationalContext);
            state.put(contextual, instanceHandle);
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
        return STATE.get() != null;
    }

    @Override
    public void destroy() {
        if (isActive()) {
            Optional.ofNullable(STATE.get()).ifPresent(ContentProcessingContextState::destroy);
        }
        STATE.remove();
    }

    @Override
    public ContextState getState() {
        if (!isActive()) {
            throw new ContextNotActiveException("No active transaction on the current thread");
        }
        return Optional.ofNullable(STATE.get()).orElse(new ContentProcessingContextState());
    }

    /**
     * Container for available beans in the content processing context.
     */
    private static class ContentProcessingContextState implements ContextState {

        private final ConcurrentMap<Contextual<?>, ContextInstanceHandle<?>> beanToInstanceHandleMap = new ConcurrentHashMap<>();

        <T> void put(Contextual<T> bean, ContextInstanceHandle<T> handle) {
            beanToInstanceHandleMap.put(bean, handle);
        }

        <T> void remove(Contextual<T> bean) {
            beanToInstanceHandleMap.remove(bean);
        }


        <T> ContextInstanceHandle<T> get(Contextual<T> bean) {
            return (ContextInstanceHandle<T>) beanToInstanceHandleMap.get(bean);
        }

        void destroy() {
            for (ContextInstanceHandle<?> handle : beanToInstanceHandleMap.values()) {
                handle.destroy();
            }
            beanToInstanceHandleMap.clear();
        }

        @Override
        public Map<InjectableBean<?>, Object> getContextualInstances() {
            return beanToInstanceHandleMap.values().stream()
                    .collect(Collectors.toMap(ContextInstanceHandle::getBean, ContextInstanceHandle::get));
        }

    }

}
