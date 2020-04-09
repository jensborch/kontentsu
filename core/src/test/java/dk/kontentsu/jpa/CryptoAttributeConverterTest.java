package dk.kontentsu.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

import dk.kontentsu.configuration.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link CryptoAttributeConverter}.
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
public class CryptoAttributeConverterTest {

    private static final String DATA = "sensitiveDataæøå";

    @Mock
    private Config conf;

    @InjectMocks
    private CryptoAttributeConverter converter;

    @BeforeEach
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
