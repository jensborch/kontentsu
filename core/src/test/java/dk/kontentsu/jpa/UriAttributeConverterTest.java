package dk.kontentsu.jpa;


import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Test for {@link URIAttributeConverter}.
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
public class UriAttributeConverterTest {

    private URIAttributeConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new  URIAttributeConverter();
    }

    @Test
    public void testConvertNullToDB() {
        String result = converter.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    public void testReadNullFromDB() {
        URI result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }

}
