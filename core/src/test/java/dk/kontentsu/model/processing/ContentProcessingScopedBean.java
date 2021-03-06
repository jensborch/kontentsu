package dk.kontentsu.model.processing;

import javax.inject.Inject;

import dk.kontentsu.model.Content;
import dk.kontentsu.spi.ContentProcessingScoped;

/**
 * CDI bean for testing {@link InjectableContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentProcessingScopedBean {

    @Inject
    private Content content;

    public Content getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
