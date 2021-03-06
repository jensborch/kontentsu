package dk.kontentsu.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

import dk.kontentsu.configuration.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link CryptoAttributeConverter}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoAttributeConverterTest {

    private static final String DATA = "sensitiveDataæøå";

    @Mock
    private Config conf;

    @InjectMocks
    private CryptoAttributeConverter converter;

    @Before
    public void setUp() {
        when(conf.dbEncryptionKey()).thenReturn("Pa$$w0rd");
    }

    @Test
    public void testConvert() {
        String result = converter.convertToDatabaseColumn(DATA);
        assertNotEquals(DATA, result);
        result = converter.convertToEntityAttribute(result);
        assertEquals(DATA, result);
    }

}
