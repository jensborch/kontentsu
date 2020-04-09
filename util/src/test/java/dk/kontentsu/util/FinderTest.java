package dk.kontentsu.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FinderTest {

    private Exception cause;
    private Throwable throwable;

    @BeforeEach
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
