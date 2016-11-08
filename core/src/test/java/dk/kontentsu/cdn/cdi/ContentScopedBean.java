package dk.kontentsu.cdn.cdi;

import javax.inject.Inject;

import dk.kontentsu.cdn.model.Content;

/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentScoped
public class ContentScopedBean {

    @Inject
    private Content content;

    public Content getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
