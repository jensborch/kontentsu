package dk.kontentsu.cdn.spi;

import javax.inject.Inject;


/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentScoped
public class ContentScopedBean {

    @Inject
    private Parsable content;

    public Parsable getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
