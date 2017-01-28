package dk.kontentsu.oauth2.module;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
    public void tetsIsMandatory() {
        Map map = new HashMap();
        map.put("javax.security.auth.message.MessagsePolicy.isMandatory", "true");
        Options options = new Options(map);
        assertTrue(options.isMandatory());
    }

    @Test
    public void testSignatureKey() {
        Options options = new Options().setSignatureKey("junit");
        assertArrayEquals("junit".getBytes(), (byte[]) options.getSignatureKey());
        assertEquals("junit", options.asMap().get("oauth2.jwt.signature.key"));
    }
}
