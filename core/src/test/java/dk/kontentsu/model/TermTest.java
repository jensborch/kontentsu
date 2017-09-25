package dk.kontentsu.model;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class TermTest {

    private Term term1;
    private Term term2;
    private Term term3;
    private Term term4;

    @Before
    public void setup() {
        term1 = new Term("uri").append("test1/");
        term2 = new Term("uri").append("/Test1/test2/");
        term3 = new Term("uri").append("test1/Test2/test3");
        term4 = new Term("color").append("blue");
    }

    @Test
    public void testItem() {
        Item i = new Item();
        term3.addItem(i);
        assertEquals(1, term3.getItems().size());
        term3.removeItem(i);
        assertEquals(0, term3.getItems().size());
    }

    @Test
    public void testFullPath() {
        assertEquals("uri:/test1/", term1.getFullPath());
        assertEquals("uri:/test1/test2/", term2.getFullPath());
        assertEquals("uri:/test1/test2/test3/", term3.getFullPath());
        assertEquals(term3.toString(), term3.getFullPath());
    }

    @Test
    public void testNames() {
        assertArrayEquals(new String[]{"test1"}, term1.getNames());
        assertArrayEquals(new String[]{"test1", "test2"}, term2.getNames());
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, term3.getNames());
    }

    @Test
    public void testAppend() {
        Term test = term1.append("test2");
        assertEquals(term2.getFullPath(), test.getFullPath());
        assertEquals(term3.getFullPath(), term1.append("test2/test3").getFullPath());
        Term testTerm = new Term("test2").append(new Term("TEST3"));
        term1.append(testTerm.getParent());
        assertEquals(term3.getFullPath(), testTerm.getFullPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendThis() {
        term1.append(term1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendWithChild() {
        term1.append(term2  );
    }

    @Test
    public void testGetTaxonomy() {
        assertEquals("uri", term1.getTaxonomy().getName());
        assertEquals("uri", term3.getTaxonomy().getName());
    }

    @Test
    public void testgetPath() {
        assertEquals("/", term1.getTaxonomy().getPath());
        assertEquals("/test1/", term1.getPath());
        assertEquals("/test1/test2/", term2.getPath());
        assertEquals("/test1/test2/test3/", term3.getPath());
    }

    @Test
    public void testIsUri() {
        assertTrue(term1.isUri());
        assertTrue(term2.isUri());
        assertTrue(term3.isUri());
        assertFalse(term4.isUri());
    }

    @Test
    public void testTaxonomy() {
        Term t = term1.getParent();
        assertEquals("uri", t.getName());
        assertEquals(0, t.getNames().length);
        assertEquals("/", t.getPath());
        assertNull(t.getParent());
    }

    @Test
    public void testIsTaxonomy() {
        assertFalse(term1.isTaxonomy());
        assertFalse(term2.isTaxonomy());
        assertFalse(term3.isTaxonomy());
        assertFalse(term4.isTaxonomy());
        assertTrue(term1.getParent().isTaxonomy());
        assertTrue(term3.getParent().getParent().getParent().isTaxonomy());
    }

    @Test
    public void testRemove() {
        Term t = term3.getParent().remove(term3);
        assertEquals(0, t.getChildren().size());
        assertEquals("uri:/test1/test2/", t.getFullPath());
        assertNull(term3.getParent());
        assertEquals("test3:/", term3.getFullPath());

    }

    @Test
    public void testPostLoad() {
        term3.initPath();
        term3.updatePathNames();
        assertEquals("uri:/test1/test2/test3/", term3.getFullPath());
    }
}
