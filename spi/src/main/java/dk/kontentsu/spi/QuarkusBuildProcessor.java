package dk.kontentsu.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.deployment.ContextRegistrarBuildItem;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

/**
 * Quarkus build processor for content processing scoped.
 */
public class QuarkusBuildProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusBuildProcessor.class);

    @BuildStep
    @SuppressWarnings("unchecked")
    public void contentProcessingContext(BuildProducer<ContextRegistrarBuildItem> contextRegistry) {
        LOGGER.info("Registring the content processing scope");
        contextRegistry.produce(new ContextRegistrarBuildItem(new ContextRegistrar() {
            @Override
            public void register(RegistrationContext registrationContext) {
                registrationContext.configure(ContentProcessingScoped.class).normal().contextClass(QuarkusContentProcessingContext.class)
                        .done();
            }
        }, ContentProcessingScoped.class));
    }

}