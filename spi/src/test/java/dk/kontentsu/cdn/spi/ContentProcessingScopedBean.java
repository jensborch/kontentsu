package dk.kontentsu.cdn.spi;

import javax.inject.Inject;


/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentProcessingScopedBean {

    @Inject
    private Parsable content;

    public Parsable getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
