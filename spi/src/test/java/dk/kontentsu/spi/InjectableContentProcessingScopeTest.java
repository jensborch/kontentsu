package dk.kontentsu.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import javax.inject.Inject;

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
@AddPackages({ContentBean.class, TestContent.class, ContentProducer.class})
@AddExtensions({ContentProcessingExtension.class})
public class InjectableContentProcessingScopeTest {

    @Inject
    private InjectableContentProcessingScope scope;

    @Inject
    private ContentBean bean;

    private ScopedContent content;

    @BeforeEach
    public void setUp() {
        content = new TestContent(1, "test");
    }

    @Test
    public void testScope() {
        final StringBuilder result = new StringBuilder();
        scope.execute(()
                -> result.append(bean.uppercase()),
                content);
        assertNotNull(result);
        assertEquals("TEST", result.toString());
    }

    @Test
    public void testNestedScope() {
        scope.execute(() -> {
            UUID outerContentId = bean.getContent().getUuid();
            scope.execute(()
                    -> assertEquals(outerContentId, bean.getContent().getUuid())
            );
        }, content);
    }

    @Test
    public void testNotActive() {
        assertThrows(ContextNotActiveException.class, () -> bean.uppercase());
    }

}
