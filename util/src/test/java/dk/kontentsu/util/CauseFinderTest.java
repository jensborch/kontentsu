package dk.kontentsu.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CauseFinderTest {

    private Exception cause;
    private Throwable throwable;

    @BeforeEach
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
