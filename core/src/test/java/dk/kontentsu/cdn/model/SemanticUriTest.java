package dk.kontentsu.cdn.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jens Borch Christiansen
 */
public class SemanticUriTest {

    private SemanticUri uri;

    @Before
    public void setUp() {
        uri = new SemanticUri(new SemanticUriPath("hej1", "hej2", "hej3", "hej4", "test"), "test");
    }

    @Test
    public void testGetPath() {
        assertEquals(5, uri.getPath().getElements().length);
        assertEquals("test", uri.getName());
    }

    @Test
    public void testToString() {
        assertEquals("hej1/hej2/hej3/hej4/test/test", uri.toString());
    }

    @Test
    public void testParse() {
        assertEquals(uri, SemanticUri.parse("hej1/hej2/hej3/hej4/test"));
        assertEquals("test", uri.getName());
        assertEquals("hej1/hej2/hej3/hej4/test", uri.getPath().toString());
    }

    @Test
    public void testParseWithName() {
        uri = SemanticUri.parse("test/test/test-xl");
        assertEquals("test-xl", uri.getName());
        assertEquals("test/test", uri.getPath().toString());
    }

    @Test
    public void testGetElements() {
        assertEquals(6, uri.getElements().length);
        assertEquals("hej1", uri.getElements()[0]);
    }

    @Test
    public void testToPath() {
        assertEquals("hej1", uri.toPath(MimeType.APPLICATION_HAL_JSON_TYPE).getName(0).toString());
        assertEquals("test.xml", uri.toPath(MimeType.APPLICATION_XML_TYPE).getName(5).toString());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        SemanticUri.parse(null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() {
        SemanticUri.parse("test/");
    }

}
