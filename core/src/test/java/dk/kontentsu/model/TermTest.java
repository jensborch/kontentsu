package dk.kontentsu.model;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TermTest {

    private Term root;
    private Term term1;
    private Term term2;
    private Term term3;
    private Term term4;
    private Term empty;

    @Before
    public void setup() {
        term1 = new Term("uri").append("test1/");
        term2 = new Term("uri").append("/test1/test2/");
        root = new Term("uri");
        term3 = root.append("test1/test2/test3");
        term4 = new Term("color").append("blue");
        empty = new Term("empty");
    }

    @Test
    public void testGetChildren() {
        assertEquals(1, root.getChildren().size());
        assertEquals(0, term3.getChildren().size());
        assertEquals(3, root.getChildren(true).size());
    }

    @Test
    public void testParse() {
        assertEquals(term3.getPathWithTaxonomy(), Term.parse("uri:/test1/test2/test3/").getPathWithTaxonomy());
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
        assertEquals("uri:/test1/", term1.getPathWithTaxonomy());
        assertEquals("uri:/test1/test2/", term2.getPathWithTaxonomy());
        assertEquals("uri:/test1/test2/test3/", term3.getPathWithTaxonomy());
        assertEquals(term3.toString(), term3.getPathWithTaxonomy());
    }

    @Test
    public void testNames() {
        assertArrayEquals(new String[]{"test1"}, term1.getElements());
        assertArrayEquals(new String[]{"test1", "test2"}, term2.getElements());
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, term3.getElements());
    }

    @Test
    public void testAppend() {
        Term test = term1.append("test2");
        assertEquals(term2.getPathWithTaxonomy(), test.getPathWithTaxonomy());
        assertEquals(term3.getPathWithTaxonomy(), term1.append("test2/test3").getPathWithTaxonomy());
        Term testTerm = new Term("test2").append(new Term("test3"));
        term1.append(testTerm.getParent().get());
        assertEquals(term3.getPathWithTaxonomy(), testTerm.getPathWithTaxonomy());
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
    public void testGetPath() {
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
        Term t = term1.getParent().get();
        assertEquals("uri", t.getName());
        assertEquals(0, t.getElements().length);
        assertEquals("/", t.getPath());
        assertFalse(t.getParent().isPresent());
    }

    @Test
    public void testIsTaxonomy() {
        assertFalse(term1.isTaxonomy());
        assertFalse(term2.isTaxonomy());
        assertFalse(term3.isTaxonomy());
        assertFalse(term4.isTaxonomy());
        assertTrue(term1.getParent().get().isTaxonomy());
        assertTrue(term3.getParent().get().getParent().get().getParent().get().isTaxonomy());
    }

    @Test
    public void testEmpty() {
        assertEquals("empty:/", empty.getPathWithTaxonomy());
        assertEquals("/", empty.getPath());
        assertEquals("empty", empty.getName());
        assertEquals(0, empty.getElements().length);
        assertTrue(empty.isTaxonomy());
    }

    @Test
    public void testRemove() {
        Term t = term3.getParent().get().remove(term3);
        assertEquals(0, t.getChildren().size());
        assertEquals("uri:/test1/test2/", t.getPathWithTaxonomy());
        assertFalse(term3.getParent().isPresent());
        assertEquals("test3:/", term3.getPathWithTaxonomy());

    }

    @Test
    public void testPostLoad() {
        term3.initPath();
        term3.updatePathElements();
        assertEquals("uri:/test1/test2/test3/", term3.getPathWithTaxonomy());
        empty.initPath();
        empty.updatePathElements();
        assertEquals("empty:/", empty.getPathWithTaxonomy());
    }
}
