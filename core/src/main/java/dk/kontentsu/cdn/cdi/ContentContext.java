package dk.kontentsu.cdn.cdi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.enterprise.context.ContextNotActiveException;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 *
 * @author Jens Borch Christiansen
 */
public class ContentContext implements AlterableContext, Serializable {

    private static final long serialVersionUID = 2249914178328516867L;

    private final ThreadLocal<Map<Contextual<?>, Instance>> instances = new ThreadLocal<>();

    public <X> X execute(final Supplier<X> task) throws Exception {
        Map<Contextual<?>, Instance> map = instances.get();
        final boolean clean = map == null;
        if (map == null) {
            map = new HashMap<>();
            instances.set(map);
        }
        try {
            return task.get();
        } finally {
            if (clean) {
                map.values().stream().forEach(Instance::destroy);
                map.clear();
                instances.remove();
            }
        }
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        Optional.ofNullable(instances.get().get(contextual)).filter(c -> c.instance != null)
                .ifPresent(Instance::destroy);
    }

    @Override
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException(ContentScoped.class.getName() + " is not active.");
        }
        Map<Contextual<?>, Instance> localMap = instances.get();
        return (T) Optional.ofNullable(localMap.get(contextual))
                .orElseGet(() -> localMap.computeIfAbsent(contextual, c -> new Instance(contextual, creationalContext)))
                .create();
    }

    @Override
    public <T> T get(final Contextual<T> contextual) {
        if (!isActive()) {
            throw new ContextNotActiveException(ContentScoped.class.getName() + " is not active.");
        }
        return (T) Optional.ofNullable(instances.get().get(contextual)).map(i -> i.instance).orElse(null);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ContentScoped.class;
    }

    @Override
    public boolean isActive() {
        final boolean initialized = instances.get() != null;
        if (!initialized) {
            instances.remove();
        }
        return initialized;
    }

    private static class Instance<T> {

        private final CreationalContext<T> context;
        private final Contextual<T> contextual;

        private T instance;

        Instance(final Contextual<T> contextual, final CreationalContext<T> context) {
            this.contextual = contextual;
            this.context = context;
        }

        public T create() {
            if (instance == null) {
                this.instance = contextual.create(context);
            }
            return instance;
        }

        public void destroy() {
            if (instance == null) {
                return;
            }

            contextual.destroy(instance, context);
            instance = null;
        }
    }

}
