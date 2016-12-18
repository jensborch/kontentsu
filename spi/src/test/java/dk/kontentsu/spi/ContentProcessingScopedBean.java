package dk.kontentsu.spi;

import dk.kontentsu.spi.ContentProcessingScoped;
import dk.kontentsu.spi.Parsable;
import java.util.UUID;
import javax.inject.Inject;

/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentProcessingScopedBean {

    private static int count = 1;

    private UUID id;

    @Inject
    private Parsable content;

    public ContentProcessingScopedBean() {
        count++;
        this.id = UUID.randomUUID();
    }

    public int getCount() {
        return count;
    }

    public UUID getId() {
        return id;
    }

    public Parsable getContent() {
        return content;
    }

    public String uppercase() {
        return content.getData().toUpperCase();
    }

}
