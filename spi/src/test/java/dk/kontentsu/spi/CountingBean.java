package dk.kontentsu.spi;

import java.util.UUID;

/**
 * CDI bean for testing <code>ContentScoped</code>.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
public class CountingBean {

    private static int count = 1;

    private final UUID id;

    public CountingBean() {
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
