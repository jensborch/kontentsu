package dk.kontentsu.cdn.cdi;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.MimeType;
import java.nio.charset.StandardCharsets;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({TestContentParser.class, ContentExtension.class, ContentProducer.class})
@SuppressWarnings("unchecked")
public class ScopeTest {

    @Inject
    private TestContentParser parser;

    @Test
    public void testScope() throws Exception {
        Content content = new Content(new byte[0], StandardCharsets.UTF_8, MimeType.parse("text/html"));
        Object result = ContentContext.execute(() -> {
            BeanManager bm = CDI.current().getBeanManager();
            Bean<TestContentParser> bean = (Bean<TestContentParser>) bm.getBeans(TestContentParser.class).iterator().next();
            CreationalContext<TestContentParser> ctx = bm.createCreationalContext(bean);
            return bm.getReference(bean, TestContentParser.class, ctx);
        }, content);
        assertNotNull(result);
        assertTrue(result instanceof TestContentParser);
    }

    @Test
    public void testScope2() throws Exception {
        Content content = new Content(new byte[0], StandardCharsets.UTF_8, MimeType.parse("text/html"));
        Object result = ContentContext.execute(() -> {
            String tmp = parser.parse();
            return tmp;
        }, content);
        assertNotNull(result);
        assertEquals("", result);
    }

}
