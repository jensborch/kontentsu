package dk.kontentsu.spi;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@EnableAutoWeld
@AddPackages({CountingBean.class, TestContent.class, ContentProducer.class})
@AddExtensions(ContentProcessingExtension.class)
public class ContentProcessingContextTest {

    @Inject
    private CountingBean bean;

    private StartableContentContext scope;

    @BeforeEach
    public void setup() {
        scope = ContentProcessingContextManager.getInstance().context();
    }

    @Test
    public void testScope() {
        UUID id;
        try {
            scope.enter(null);
            assertNotNull(bean);
            id = bean.getId();
        } finally {
            scope.exit();
        }
        assertNotNull(id);
    }

    @Test
    public void testNestedScope() {
        try {
            scope.enter(null);
            final int count = bean.getCount();
            UUID outerId = bean.getId();
            assertNotEquals(1, count);
            try {
                scope.enter(null);
                assertNotEquals(2, bean.getCount());
                assertNotEquals(outerId, bean.getId());
                try {
                    scope.enter(null);
                    assertNotEquals(3, bean.getCount());
                    assertNotEquals(outerId, bean.getId());
                } finally {
                    scope.exit();
                }
            } finally {
                scope.exit();
            }
        } finally {
            scope.exit();
        }
    }

    @Test
    public void testNotActive() {
        ContextNotActiveException e = assertThrows(ContextNotActiveException.class, () -> bean.getId());
        assertNotNull(e);
    }

}
