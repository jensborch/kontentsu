package dk.kontentsu.model.processing;

import dk.kontentsu.model.processing.ContentProcessingScopedBean;
import dk.kontentsu.model.processing.InjectableContentProcessingScope;
import dk.kontentsu.model.Content;
import dk.kontentsu.spi.ContentProcessingExtension;
import dk.kontentsu.model.MimeType;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.weld.context.ContextNotActiveException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link InjectableContentProcessingScope}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ContentProcessingScopedBean.class, ContentProcessingExtension.class, ContentProducer.class})
public class InjectableContentProcessingScopeTest {

    @Inject
    private ContentProcessingScopedBean bean;

    private Content content;

    @Before
    public void setUp() {
        content = new Content("scope test".getBytes(), StandardCharsets.UTF_8, MimeType.parse("text/plain"));
    }

    @Test
    public void testScope() throws Exception {
        final StringBuilder result = new StringBuilder();
        InjectableContentProcessingScope.execute(() -> {
            result.append(bean.uppercase());
        }, content);
        assertNotNull(result);
        assertEquals("SCOPE TEST", result.toString());
    }

    @Test
    public void testNestedScope() throws Exception {
        InjectableContentProcessingScope.execute(() -> {
            UUID outerContentId = bean.getContent().getUuid();
            InjectableContentProcessingScope.execute(() -> {
                assertEquals(outerContentId, bean.getContent().getUuid());
            });
        }, content);
    }

    @Test(expected = ContextNotActiveException.class)
    public void testNotActive() throws Exception {
        bean.uppercase();
    }

}