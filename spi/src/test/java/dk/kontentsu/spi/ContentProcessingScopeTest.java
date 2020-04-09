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
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@EnableAutoWeld
@AddPackages(ContentProcessingScopedBean.class)
@AddExtensions(ContentProcessingExtension.class)
public class ContentProcessingScopeTest {

    @Inject
    private ContentProcessingScopedBean bean;

    @Test
    public void testScope() {
        UUID id;
        try (ContentProcessingScope scope = new ContentProcessingScope()) {
            scope.start();
            assertNotNull(bean);
            id = bean.getId();
        }
        assertNotNull(id);
    }

    @Test
    public void testNestedScope() {
        try (ContentProcessingScope scope1 = new ContentProcessingScope()) {
            scope1.start();
            final int count = bean.getCount();
            UUID outerId = bean.getId();
            assertNotEquals(1, count);
            try (ContentProcessingScope scope2 = new ContentProcessingScope()) {
                scope2.start();
                assertNotEquals(2, bean.getCount());
                assertNotEquals(outerId, bean.getId());
                try (ContentProcessingScope scope3 = new ContentProcessingScope()) {
                    scope3.start();
                    assertNotEquals(3, bean.getCount());
                    assertNotEquals(outerId, bean.getId());
                }
            }
        }
    }

    @Test
    public void testNotActive() {
        ContextNotActiveException e = assertThrows(ContextNotActiveException.class, () -> bean.getId());
        assertNotNull(e);
    }

}
