package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemURITest {

    private Item.URI uri;

    @BeforeEach
    public void setup() {
        Term term = Term.create("uri").append("test1/test2");
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

    @Test
    public void testUnknownExtension() {
        assertThrows(IllegalArgumentException.class, () -> new Item.URI("/test1/test2/test2-test.test"));
    }

    @Test
    public void testIllegalUri() {
        assertThrows(IllegalArgumentException.class, () -> new Item.URI("/test1/test2/test3-test.gif"));
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
