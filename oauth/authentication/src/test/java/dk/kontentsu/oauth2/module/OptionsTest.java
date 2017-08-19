package dk.kontentsu.oauth2.module;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link Options}.
 *
 * @author Jens Borch Christiansen
 */
public class OptionsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testIsMandatory() {
        Map map = new HashMap();
        map.put("javax.security.auth.message.MessagsePolicy.isMandatory", "true");
        Options options = new Options(map);
        assertTrue(options.isMandatory());
    }

    @Test
    public void testSignatureKey() throws Exception {
        Options options = new Options().setSignatureKey("junit");
        assertArrayEquals("junit".getBytes(), options.getSignatureKey());
        assertEquals("junit", options.asMap().get("oauth2.jwt.signature.key"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAugment() throws Exception {
        Options options = new Options().setSignatureKey("junit");
        Map map = new HashMap();
        map.put("javax.security.auth.message.MessagsePolicy.isMandatory", "wrong");
        map.put("test", "junit");
        options.augment(map);
        assertArrayEquals("junit".getBytes(), options.getSignatureKey());
        assertFalse(options.isMandatory());
        assertEquals("junit", options.asMap().get("test"));
    }
}
