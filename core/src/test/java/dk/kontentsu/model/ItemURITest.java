package dk.kontentsu.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ItemURITest {

    private Item.URI uri;

    @Before
    public void setup() {
        Term term = new Term("uri").append("test1/test2");
        uri = new Item.URI(new Item(term, "test"));
    }

    @Test
    public void testConstructor() {
        assertEquals(uri, new Item.URI("/test1/test2/test"));
    }

    @Test
    public void testGetElements() {
        String[] elements = uri.getPathElements();
        assertEquals(3, elements.length);
        assertArrayEquals(new String[] {"test1", "test2", "test"} , elements);
    }

    @Test
    public void testGetPath() {
        assertEquals("/test1/test2/", uri.getPath());
    }

    @Test
    public void testToString() {
        assertEquals("/test1/test2/test", uri.toString());
    }
}
