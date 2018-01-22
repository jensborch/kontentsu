package dk.kontentsu.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;


/**
 * Test for {@link Version}.
 *
 * @author Jens Borch Christiansen
 */
public class VersionTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private Content content;
    private Item item;
    private Version version;

    @Before
    public void setUp() throws Exception  {
        content = new Content("This is a test".getBytes(), Charset.defaultCharset());
        Term path = Term.parse("uri:/test1/test2/");
        item = new Item(path, new MimeType("text", "plain"));
        version = Version.builder()
                .from(NOW.plusDays(2))
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .content(content)
                .build();
        item.addVersion(version);
    }

    @Test
    public void testRelations() {
        assertEquals(1, item.getVersions().size());
        assertNotNull(version.getItem());
        assertEquals(item, version.getItem());
    }

    @Test
    public void testIsComplete() {
        assertTrue(version.isComplete());
        Term path = Term.parse("uri:/test2/test3/");
        Item compItem = new Item(path, MimeType.APPLICATION_JSON_TYPE);
        Version compVersion = Version.builder()
                .from(NOW.plusDays(2))
                .content(content)
                .reference(item, ReferenceType.COMPOSITION)
                .build();
        compItem.addVersion(compVersion);
        assertTrue(compVersion.isComplete());
    }

    @Test
    public void testIsNotComplete() {
        assertTrue(version.isComplete());
        Term path = Term.parse("uri:/test2/test3/");
        Item compItem = new Item(path, MimeType.APPLICATION_JSON_TYPE);
        Version compVersion = Version.builder()
                .from(NOW.minusDays(2))
                .to(NOW.minusSeconds(1))
                .content(content)
                .reference(item, ReferenceType.COMPOSITION)
                .build();
        compItem.addVersion(compVersion);
        assertFalse(compVersion.isComplete());
    }

}
