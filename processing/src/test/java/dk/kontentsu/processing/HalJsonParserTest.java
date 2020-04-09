package dk.kontentsu.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link HalJsonParser}
 *
 * @author Jens Borch Christiansen
 */
public class HalJsonParserTest {

    private ContentTestData data;

    @BeforeEach
    public void setUp() {
        data = new ContentTestData();
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8);
        HalJsonParser parser = new HalJsonParser();
        parser.content = content;

        ContentParser.Results result = parser.parse();

        assertEquals(7, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.PAGE, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.PAGE, "author")).getValue());
        assertEquals(5, result.getLinks().size());

    }

}
