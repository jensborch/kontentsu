package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link MetadataType}
 *
 * @author Jens Borch Christiansen
 */
public class MetadataTypeTest {

    @Test
    public void testParse() {
        assertEquals(MetadataType.OTHER, MetadataType.parse("unknown-value"));
        assertEquals(MetadataType.OTHER, MetadataType.parse(""));
        assertEquals(MetadataType.OTHER, MetadataType.parse(null));
        assertEquals(MetadataType.OTHER, MetadataType.parse("other"));
        assertEquals(MetadataType.PAGE, MetadataType.parse("page"));
    }

}
