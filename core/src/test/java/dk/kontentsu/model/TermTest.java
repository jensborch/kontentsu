package dk.kontentsu.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TermTest {

    @Test
    public void testFullPath() {
        Term term = new Term("uri").append("test1/test2");
        String path = term.getFullPath();
        assertEquals("uri:/test1/test2", path);
    }
}
