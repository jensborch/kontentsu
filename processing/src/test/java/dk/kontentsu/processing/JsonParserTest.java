package dk.kontentsu.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link JsonParser}
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class
)
public class JsonParserTest {

    private ContentTestData data;

    @Before
    public void setup() {
        data = new ContentTestData(ContentTestData.Type.JSON);
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8, MimeType.APPLICATION_JSON_TYPE);
        JsonParser parser = new JsonParser();
        Whitebox.setInternalState(parser, "content", content);
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
