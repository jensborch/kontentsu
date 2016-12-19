package dk.kontentsu.scope;

import dk.kontentsu.model.Content;
import dk.kontentsu.spi.ContentProcessingScoped;
import javax.inject.Inject;

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
