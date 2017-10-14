package dk.kontentsu.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class CauseFinderTest {

    private Exception cause;
    private Throwable throwable;

    @Before
    public void setup() {
        cause = new Exception("test");
        throwable = new Exception(new Exception(new Exception(cause)));
    }

    @Test
    public void findExceptionCause() {
        Optional<Throwable> found = new CauseFinder(e -> e.getMessage().equals("test")).find(throwable);
        assertEquals(cause, found.get());
    }

    @Test
    public void findExceptionCauseNoMatch() {
        Optional<Throwable> found = new CauseFinder(e -> e.getMessage().equals("")).find(throwable);
        assertFalse(found.isPresent());
    }

}
