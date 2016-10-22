package dk.kontentsu.cdn.cdi;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 *
 * @author Jens Borch Christiansen
 */
public class ContentExtension implements Extension, Serializable {

    private static final long serialVersionUID = -2791994163919507379L;

    public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
        event.addScope(ContentScoped.class, true, false);
    }

    public void afterBeanDiscovery(@Observes final AfterBeanDiscovery event) {
        event.addContext(new ContentContext());
    }

    <T> void processInjectionTarget(@Observes final ProcessInjectionTarget<T> pit) {

    }

}
