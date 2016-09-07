package dk.kontentsu.cdn.jpa;


import dk.kontentsu.cdn.jpa.URIAttributeConverter;

import static org.junit.Assert.assertNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Test for {@link URIAttributeConverter}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class UriAttributeConverterTest {

    private URIAttributeConverter converter;

    @Before
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
