package dk.kontentsu.cdn.parsers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.internal.Metadata;
import dk.kontentsu.cdn.model.internal.MetadataType;
import dk.kontentsu.cdn.upload.ContentTestData;

/**
 * Test for {@link HalJsonParser}
 *
 * @author JensBorch
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
        assertTrue(result.getMetadata().containsKey(new Metadata.Key(MetadataType.SEO, "author")));
        assertEquals("Peter Nielsen", result.getMetadata().get(new Metadata.Key(MetadataType.SEO, "author")).getValue());
        assertEquals(5, result.getLinks().size());

    }

}
