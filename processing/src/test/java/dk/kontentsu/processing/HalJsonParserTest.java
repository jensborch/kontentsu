package dk.kontentsu.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link HalJsonParser}
 *
 * @author Jens Borch Christiansen
 */
public class HalJsonParserTest {

    private ContentTestData data;

    @Before
    public void setUp() {
        data = new ContentTestData();
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8, MimeType.APPLICATION_HAL_JSON_TYPE);
        HalJsonParser parser = new HalJsonParser();
        parser.content = content;

        ContentParser.Results result = parser.parse();

        assertEquals(7, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.PAGE, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.PAGE, "author")).getValue());
        assertEquals(5, result.getLinks().size());

    }

}
