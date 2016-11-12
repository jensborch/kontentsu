package dk.kontentsu.cdn.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;

import org.jboss.weld.context.ContextNotActiveException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ContentScoped}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ContentScopedBean.class, ContentExtension.class, ContentProducer.class})
public class ScopeTest {

    @Inject
    private ContentScopedBean bean;

    @Test
    public void testScope() throws Exception {
        Parsable content = new Parsable() {

            private final static String data = "scope test";

            @Override
            public String getData() {
                return data;
            }

            @Override
            public byte[] getDataAsBytes() {
                return data.getBytes();
            }

            @Override
            public Optional<Charset> getEncoding() {
                return Optional.of(StandardCharsets.UTF_8);
            }
        };
        Object result = ContentContext.execute(() -> {
            return bean.uppercase();
        }, content);
        assertNotNull(result);
        assertEquals("SCOPE TEST", result);
    }

    @Test(expected = ContextNotActiveException.class)
    public void testNotActive() throws Exception {
        bean.uppercase();
    }

}
