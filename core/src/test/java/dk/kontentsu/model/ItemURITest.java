package dk.kontentsu.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class ItemURITest {

    private Item.URI uri;

    @Before
    public void setup() {
        Term term = new Term("uri").append("test1/test2");
        uri = new Item.URI(new Item(term, "test", MimeType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testConstructor() {
        assertEquals(uri, new Item.URI("/test1/test2/test2-test"));
        assertEquals(uri, new Item.URI("test1/test2/test2-test"));
        assertEquals(uri, new Item.URI("/test1/test2/test2-test.json"));
        assertNotEquals(uri, new Item.URI("/test1/test2/test2-test.gif"));
    }

    @Test
    public void testGetName() {
        assertEquals("test", uri.getEdition().orElse(""));
    }

    @Test
    public void testSpecialChar() {
        assertEquals("/test1/\u00E6/\u00E6-test.json", new Item.URI("/test1/\u00E6/\u00E6-test").toString());
        assertEquals("/test1/t-t/t-t-test.json", new Item.URI("/test1/t-t/t-t-test").toString());
    }

    @Test
   public void testMatches() {
        assertTrue(uri.matches("test1/test2/test2-test"));
        assertFalse(uri.matches("test1/test3/"));
        assertFalse(uri.matches("i1"));
    }

    @Test
    public void testGetMimeType() {
        assertEquals(MimeType.APPLICATION_JSON_TYPE, uri.getMimeType());
        assertEquals(MimeType.IMAGE_PNG_TYPE, new Item.URI("images/i1/i1.png").getMimeType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknowExtension() {
        new Item.URI("/test1/test2/test2-test.test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalUri() {
        new Item.URI("/test1/test2/test3-test.gif");
    }

    @Test
    public void testGetElements() {
        String[] elements = uri.getPathElements();
        assertArrayEquals(new String[]{"test1", "test2", "test2-test.json"}, elements);
    }

    @Test
    public void testGetPath() {
        assertEquals("/test1/test2/", uri.getFolder());
    }

    @Test
    public void testToString() {
        assertEquals("/test1/test2/test2-test.json", uri.toString());
    }

    @Test
    public void testToPath() {
        assertEquals(Paths.get("test1", "test2", "test2-test.json"), uri.toPath());
    }

    @Test
    public void testNotEquals() {
        assertNotEquals(null, uri);
        assertNotEquals(new Object(), uri);
    }

    @Test
    public void tesEquals() {
        assertEquals(uri, uri);
    }
}
