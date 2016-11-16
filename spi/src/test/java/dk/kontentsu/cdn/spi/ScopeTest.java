package dk.kontentsu.cdn.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

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

            private final static String DATA = "scope test";

            @Override
            public String getData() {
                return DATA;
            }

            @Override
            public InputStream getDataAsBinaryStream() {
                throw new UnsupportedOperationException("Not supported for test");
            }

            @Override
            public Optional<Charset> getEncoding() {
                return Optional.of(StandardCharsets.UTF_8);
            }

            @Override
            public UUID getUuid() {
                throw new UnsupportedOperationException("Not supported for test");
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
