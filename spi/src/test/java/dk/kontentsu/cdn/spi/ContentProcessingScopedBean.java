package dk.kontentsu.cdn.spi;

import javax.inject.Inject;

/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentProcessingScopedBean {

    private static int count = 1;

    @Inject
    private Parsable content;

    public ContentProcessingScopedBean() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public Parsable getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
