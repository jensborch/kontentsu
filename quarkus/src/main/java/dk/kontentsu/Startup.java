package dk.kontentsu;


import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import dk.kontentsu.spi.ContentProcessingScoped;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ApplicationScoped
public class Startup {

    private static final Logger LOGGER = LoggerFactory.getLogger(Startup.class);

    @PostConstruct
    public void init() {
        io.quarkus.arc.Arc.container().getScopes().forEach(s -> LOGGER.info("Found scope {}", s));
    }

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");
    }

    public void contextInit(@Observes @Initialized(ContentProcessingScoped.class) Object obj) {
        LOGGER.info("Found context {}", obj);
    }

}
