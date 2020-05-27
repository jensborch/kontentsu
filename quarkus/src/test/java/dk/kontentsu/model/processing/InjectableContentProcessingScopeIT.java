package dk.kontentsu.model.processing;

import dk.kontentsu.spi.InjectableContentProcessingScope;
import dk.kontentsu.spi.ScopedContent;

import static io.codearte.catchexception.shade.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import dk.kontentsu.model.Content;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link InjectableContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@QuarkusTest
public class InjectableContentProcessingScopeIT {

    @Inject
    private InjectableContentProcessingScope scope;

    @Inject
    private ContentBean bean;

    private ScopedContent content;

    @BeforeEach
    public void setUp() {
        content = new Content("scope test".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Test
    public void testScope() {
        final StringBuilder result = new StringBuilder();
        scope.execute(()
                -> result.append(bean.uppercase()),
                content);
        assertNotNull(result);
        assertEquals("SCOPE TEST", result.toString());
    }

    @Test
    public void testNestedScope() {
        ScopedContent nested = new Content("scope test".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        scope.execute(() -> {
            scope.execute(()
                    -> assertEquals(nested.getUuid(), bean.getContent().getUuid()),
                    nested);
        }, content);
    }

    @Test
    public void testNestedScopeOuterContent() {
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
