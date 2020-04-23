package dk.kontentsu.spi;

import javax.enterprise.context.spi.AlterableContext;

/**
 *
 */
public interface StartableContentContext extends AlterableContext {

    void enter(final ScopedContent content);

    void exit();

    ScopedContent getScopedContent();

}
