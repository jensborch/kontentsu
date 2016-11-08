package dk.kontentsu.cdn.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.jboss.weld.context.ContextNotActiveException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.MimeType;

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
        Content content = new Content("Scope test".getBytes(), StandardCharsets.UTF_8, MimeType.parse("plain/text"));
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
