package dk.kontentsu.spi;

import dk.kontentsu.spi.ContentProcessingScoped;
import java.util.UUID;

/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class ContentProcessingScopedBean {

    private static int count = 1;

    private UUID id;

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

}
