
package dk.kontentsu.cdn.model;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;

import org.junit.Test;

/**
 * Test for {@link dk.kontentsu.cdn.model.Content}
 *
 * @author Jens Borch Christiansen
 */
public class ContentTest {

    @Test
    public void testByteArrayHashAndSize() {
        final Charset charset = Charset.forName("UTF-8");
        Content content = new Content("Test test".getBytes(charset), charset, new MimeType("plain/text"));
        assertEquals("180fed764dbd593f1ea45b63b13d7e69", content.getHash());
        assertEquals(9, content.getSize());
    }

    @Test
    public void testStreamHash() {
        final Charset charset = Charset.forName("UTF-8");
        final byte[] data = "Test test".getBytes(charset);
        Content content = new Content(data, charset, new MimeType("plain/text"));
        assertEquals("180fed764dbd593f1ea45b63b13d7e69", content.getHash());
    }
}
