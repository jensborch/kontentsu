package dk.kontentsu.cdn.cdi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    public Object execute(final Supplier task) throws Exception {
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T get(final Contextual<T> component) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
