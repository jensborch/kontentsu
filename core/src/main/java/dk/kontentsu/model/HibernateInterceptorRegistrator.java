package dk.kontentsu.model;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;

import io.quarkus.runtime.StartupEvent;

/**
 * Class responsible for registering Hibernate event listeners.
 */
@ApplicationScoped
public class HibernateInterceptorRegistrator {

    @Inject
    EntityManagerFactory factory;

    void onStart(@Observes StartupEvent ev) {
        EventListenerRegistry registry = factory.unwrap(SessionFactoryImpl.class).getServiceRegistry()
                .getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_UPDATE, ModifiedListener.class);
    }

}