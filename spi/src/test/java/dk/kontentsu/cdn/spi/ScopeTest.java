package dk.kontentsu.cdn.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ContentScoped}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ContentProcessingScopedBean.class, ContentExtension.class, ContentProducer.class})
public class ScopeTest {

    @Inject
    private ContentProcessingScopedBean bean;

    private Parsable content;

    @Before
    public void setUp() {
        content = new Parsable() {

            private final static String DATA = "scope test";

            private final UUID id = UUID.randomUUID();

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
                return id;
            }

            @Override
            public MimeType getMimeType() {
                return MimeType.APPLICATION_JSON_TYPE;
            }
        };
    }

    @Test
    public void testScope() throws Exception {
        final StringBuilder result = new StringBuilder();
        ContentContext.execute(() -> {
            result.append(bean.uppercase());
        }, content);
        assertNotNull(result);
        assertEquals("SCOPE TEST", result.toString());
    }

    @Test
    public void testNestedScope() throws Exception {
        ContentContext.execute(() -> {
            final int count = bean.getCount();
            UUID outerContentId = bean.getContent().getUuid();
            UUID outerId = bean.getId();
            assertNotEquals(1, count);
            ContentContext.execute(() -> {
                assertEquals(outerContentId, bean.getContent().getUuid());
                assertNotEquals(2, bean.getCount());
                assertNotEquals(outerId, bean.getId());
            });
        }, content);
    }

    @Test(expected = ContextNotActiveException.class)
    public void testNotActive() throws Exception {
        bean.uppercase();
    }

}
