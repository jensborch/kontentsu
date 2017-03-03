package dk.kontentsu.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.internal.Metadata;
import dk.kontentsu.model.internal.MetadataType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import dk.kontentsu.test.ContentTestData.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

/**
 * Test for {@link JsonParser}
 *
 * @author Jens Borch Christiansen
 */
public class JsonParserTest {

    private ContentTestData data;

    @Before
    public void setUp() {
        data = new ContentTestData(Type.JSON);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8, MimeType.APPLICATION_JSON_TYPE);
        JsonParser parser = new JsonParser();
        Whitebox.setInternalState(parser, "content", content);

        ContentParser.Results result = parser.parse();

        assertEquals(6, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.PAGE, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.PAGE, "author")).getValue());
        assertEquals(6, result.getLinks().size());
    }

}
