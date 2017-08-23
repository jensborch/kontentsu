package dk.kontentsu.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TermTest {

    private Term term1;
    private Term term2;
    private Term term3;

    @Before
    public void setup() {
        term1 = new Term("uri").append("test1/");
        term2 = new Term("uri").append("/Test1/test2/");
        term3 = new Term("uri").append("test1/Test2/test3");
    }

    @Test
    public void testFullPath() {
        assertEquals("uri:/test1", term1.getFullPath());
        assertEquals("uri:/test1/test2", term2.getFullPath());
        assertEquals("uri:/test1/test2/test3", term3.getFullPath());
    }
}
