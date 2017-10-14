package dk.kontentsu.util;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class FinderTest {

    private Exception cause;
    private Throwable throwable;

    @Before
    public void setup() {
        cause = new Exception("test");
        throwable = new Exception(new Exception(new Exception(cause)));
    }

    @Test
    public void findExceptionCause() {
        Optional<Throwable> found = new Finder<>(e -> e.getMessage().equals("test"), Throwable::getCause).find(throwable);
        assertEquals(cause, found.get());
    }

    @Test
    public void findExceptionCauseNoMatch() {
        Optional<Throwable> found = new Finder<>(e -> e.getMessage().equals(""), Throwable::getCause).find(throwable);
        assertFalse(found.isPresent());
    }

    @Test
    public void findFirstException() {
        Optional<Throwable> found = new Finder<>(e -> e.getMessage().equals("test"), Throwable::getCause).find(new Exception("test"));
        assertTrue(found.isPresent());
    }

}
