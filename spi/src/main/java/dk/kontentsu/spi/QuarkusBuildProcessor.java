package dk.kontentsu.spi;

import io.quarkus.arc.deployment.ContextRegistrarBuildItem;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

/**
 * Quarkus build processor for content processing scoped.
 */
public class QuarkusBuildProcessor {

    @BuildStep
    public void contentProcessingContext(BuildProducer<ContextRegistrarBuildItem> contextRegistry) {

        contextRegistry.produce(new ContextRegistrarBuildItem(new ContextRegistrar() {
            @Override
            public void register(RegistrationContext registrationContext) {
                registrationContext.configure(ContentProcessingScoped.class).normal().contextClass(QuarkusContentProcessingContext.class)
                        .done();
            }
        }, ContentProcessingScoped.class));
    }

}