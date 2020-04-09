package dk.kontentsu.model.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

import dk.kontentsu.model.Content;
import dk.kontentsu.spi.ContentProcessingExtension;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link InjectableContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@EnableAutoWeld
@AddPackages(ContentProcessingScopedBean.class)
@AddExtensions(ContentProcessingExtension.class)
public class InjectableContentProcessingScopeTest {

    @Inject
    private ContentProcessingScopedBean bean;

    private Content content;

    @BeforeEach
    public void setUp() {
        content = new Content("scope test".getBytes(), StandardCharsets.UTF_8);
    }

    @Test
    public void testScope() {
        final StringBuilder result = new StringBuilder();
        InjectableContentProcessingScope.execute(()
                -> result.append(bean.uppercase()),
                content);
        assertNotNull(result);
        assertEquals("SCOPE TEST", result.toString());
    }

    @Test
    public void testNestedScope() {
        InjectableContentProcessingScope.execute(() -> {
            UUID outerContentId = bean.getContent().getUuid();
            InjectableContentProcessingScope.execute(()
                    -> assertEquals(outerContentId, bean.getContent().getUuid())
            );
        }, content);
    }

    @Test
    public void testNotActive() {
        assertThrows(ContextNotActiveException.class, () -> bean.uppercase());
    }

}
