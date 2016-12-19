package dk.kontentsu.spi;

import java.util.UUID;
import javax.inject.Inject;
import org.jboss.weld.context.ContextNotActiveException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ContentProcessingScopedBean.class, ContentProcessingExtension.class})
public class ContentProcessingScopeTest {

    @Inject
    private ContentProcessingScopedBean bean;

    @Test
    public void testScope() throws Exception {
        UUID id;
        try (ContentProcessingScope scope = new ContentProcessingScope()) {
            scope.start();
            assertNotNull(bean);
            id = bean.getId();
        }
        assertNotNull(id);
    }

    @Test
    public void testNestedScope() throws Exception {
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

    @Test(expected = ContextNotActiveException.class)
    public void testNotActive() throws Exception {
        bean.getId();
    }

}
