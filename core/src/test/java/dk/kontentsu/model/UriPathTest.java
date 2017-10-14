package dk.kontentsu.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jens Borch Christiansen
 */
public class UriPathTest {

    private SemanticUriPath uri;

    @Before
    public void setUp() {
        uri = new SemanticUriPath("hej1", "hej2", "hej3", "hej4");
    }

    @Test
    public void testGetPath() {
        assertEquals(4, uri.getElements().length);
        assertEquals("hej1", uri.getElements()[0]);
        assertEquals("hej4", uri.getElements()[3]);
    }

    @Test
    public void testToString() {
        assertEquals("hej1/hej2/hej3/hej4", uri.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        uri = new SemanticUriPath((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyString() {
        uri = new SemanticUriPath("");
    }

}
