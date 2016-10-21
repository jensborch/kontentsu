package dk.kontentsu.cdn.cdi;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 *
 * @author Jens Borch Christiansen
 */
public class WrappingInjectionTarget<T> implements InjectionTarget<T> {

    private final InjectionTarget<T> target;

    public WrappingInjectionTarget(final InjectionTarget<T> target) {
        this.target = target;
    }

    @Override
    public void dispose(final T instance) {
        target.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return target.getInjectionPoints();
    }

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx) {
        target.inject(instance, ctx);
    }

    @Override
    public void postConstruct(final T instance) {
        target.postConstruct(instance);
    }

    @Override
    public void preDestroy(final T instance) {
        target.preDestroy(instance);
    }

    @Override
    public T produce(final CreationalContext<T> creationalContext) {
        return target.produce(creationalContext);
    }

}
