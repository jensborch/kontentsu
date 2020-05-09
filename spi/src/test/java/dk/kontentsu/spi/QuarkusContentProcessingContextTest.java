package dk.kontentsu.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class QuarkusContentProcessingContextTest {

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testEnter() {
        QuarkusContentProcessingContext context = new QuarkusContentProcessingContext();
        context.enter(null);
        assertTrue(context.isActive());
        context.exit();
        assertFalse(context.isActive());
    }

}
