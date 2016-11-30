package dk.kontentsu.cdn.model;

import dk.kontentsu.cdn.spi.MimeType;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

/**
 * Test of {@link dk.kontentsu.cdn.model.MimeType}
 */
public class MimeTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalid() {
        MimeType.parse("test");
    }

    @Test
    public void getMediaTypeWithCharset() {
        assertEquals("application/hal+json;charset=UTF-8", (new MimeType("application", "hal+json")).toMediaTypeWithCharset().toString());
        assertEquals("application/json;charset=UTF-8", (MimeType.APPLICATION_JSON_TYPE).toMediaTypeWithCharset().toString());
        assertEquals("application/xml;charset=UTF-8", MimeType.APPLICATION_XML_TYPE.toMediaTypeWithCharset().toString());
        assertEquals("image/png", (new MimeType("image", "png").toMediaTypeWithCharset().toString()));
    }

    @Test
    public void equals() {
        assertEquals("application/hal+json", MimeType.APPLICATION_HAL_JSON_TYPE.toString());
        assertEquals(new MediaType("application", "hal+json"), MimeType.APPLICATION_HAL_JSON_TYPE.toMediaType());
        Map<String, String> params = new HashMap<>();
    }

    @Test
    public void parseHeader() {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        List<MimeType> m = MimeType.parseHeader(header);
        assertEquals(5, m.size());
    }

    @Test
    public void wildcard() {
        assertTrue("Equals should work", MimeType.parse("*/*").equals(new MimeType("*", "*")));
        assertTrue("Single wildcard should work", MimeType.parse("*").equals(MimeType.parse("*/*")));
        assertTrue("Wildcarde should match type and subtype", MimeType.parse("*/*").matches(MimeType.parse("application/json")));
    }

    @Test
    public void halJson() {
        assertTrue(MimeType.parse("application/hal+json").equals(new MimeType("application", "hal+json")));
    }

    @Test
    public void params() {
        MimeType m = MimeType.parse("*/*;q=1;v=2");
        assertNotNull(m.getParams());
        assertEquals(2, m.getParams().size());
    }

    @Test
    public void paramsLong() {
        MimeType m = MimeType.parse("*/*;q=12  ;  v=34;test=qwert123");
        assertNotNull(m.getParams());
        assertEquals(3, m.getParams().size());
        assertEquals("12", m.getParams().get("q"));
    }

    @Test
    public void match() {
        String test = "text/css,*/*;q=0.1";
        assertTrue(MimeType.parse("application/pdf").matchesHeader(test));
    }

    @Test
    public void goodMatch() {
        String test = "text/css,*/*;q=0.1";
        assertTrue(MimeType.parse("text/css").matchesHeader(test));
    }

    @Test
    public void noMatch() {
        String test = "text/css,applocation/*;q=0.1";
        assertFalse(MimeType.parse("text/html").matchesHeader(test));
    }

    @Test
    public void test2String() {
        String test = "applocation/*;q=0.1;version=42";
        assertTrue(MimeType.parse(test).toString().equals(test));
    }

    @Test
    public void testEqualsreflexive() {
        MimeType x = new MimeType("image", "gif");
        assertTrue(x.equals(x));
    }

    @Test
    public void testEqualsSymmetric() {
        MimeType x = MimeType.parse("applocation/*;q=0.1");
        MimeType y = MimeType.parse("applocation/*;q=0.1");
        assertTrue(x.equals(y) && y.equals(x));
        assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testNotEquals() {
        MimeType x = MimeType.parse("applocation/*;q=0.5");
        MimeType y = MimeType.parse("applocation/*;q=0.1");
        assertFalse(x.equals(y) && y.equals(x));
        assertFalse(x.equals(null));
        assertFalse(x.equals(""));
        assertFalse(x.equals("jsdfghæfsed"));
        assertFalse(x.equals(new Object()));
        assertFalse(x.equals(new MimeType("image", "jpeg")));
        assertFalse(MimeType.parse("application/pdf").equals("application/*"));
        assertFalse(MimeType.parse("application/pdf").equals("application/pdf"));
        assertFalse(MimeType.parse("application/pdf;q=1").equals(MimeType.parse("application/pdf;q=4")));
    }

    @Test
    public void equalsTrue() {
        assertTrue(MimeType.parse("application/pdf").equals(MimeType.parse("application/pdf")));
        assertTrue(MimeType.parse("application/pdf;q=1").equals(MimeType.parse("application/pdf;q=1")));

        Object x = MimeType.parse("application/gif");
        assertTrue((new MimeType("application", "gif")).equals(x));

        Set<MimeType> types = new HashSet<>();
        types.add(MimeType.parse("application/gif"));

        assertTrue(types.contains(new MimeType("application", "gif")));
    }

}
