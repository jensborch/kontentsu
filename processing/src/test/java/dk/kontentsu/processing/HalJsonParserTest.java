package dk.kontentsu.processing;

import dk.kontentsu.processing.HalJsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.internal.Metadata;
import dk.kontentsu.model.internal.MetadataType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.test.ContentTestData;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

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

    @After
    public void tearDown() {
    }

    @Test
    public void testParse() throws Exception {
        Content content = new Content(data.getFullPage(), StandardCharsets.UTF_8, MimeType.APPLICATION_HAL_JSON_TYPE);
        HalJsonParser parser = new HalJsonParser();
        Whitebox.setInternalState(parser, "content", content);

        ContentParser.Results result = parser.parse();

        assertEquals(7, result.getMetadata().size());
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.PAGE, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.PAGE, "author")).getValue());
        assertEquals(5, result.getLinks().size());

    }

}
