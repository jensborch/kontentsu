package dk.kontentsu.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link JsonParser}
 *
 * @author Jens Borch Christiansen
 */
public class JsonParserTest {

    private ContentTestData data;

    @BeforeEach
    public void setup() {
        data = new ContentTestData(ContentTestData.Type.JSON);
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8);
        JsonParser parser = new JsonParser();
        parser.content = content;
        parser.init();

        ContentParser.Results result = parser.parse();

        assertEquals(6, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.PAGE, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.PAGE, "author")).getValue());
        //TODO: Should not include self link
        assertEquals(5, result.getLinks().size());
        assertEquals(4L, result.getLinks()
                .stream()
                .filter(l -> l.getType() == ReferenceType.COMPOSITION)
                .count()
        );
    }

}
