package dk.kontentsu.model.processing;

import javax.inject.Inject;

import dk.kontentsu.spi.ContentProcessingScoped;
import dk.kontentsu.spi.ScopedContent;

/**
 * CDI bean for testing {@link InjectableContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentBean {

    @Inject
    ScopedContent content;

    public ScopedContent getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
