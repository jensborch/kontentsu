package dk.kontentsu.model;

import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.Content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.kontentsu.model.internal.Item;
import dk.kontentsu.model.internal.Metadata;
import dk.kontentsu.model.internal.MetadataType;
import dk.kontentsu.model.internal.ReferenceType;
import dk.kontentsu.model.internal.Version;

/**
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
        content = new Content("This is a test".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));
        //Group group = new Group("test1", "test2");
        SemanticUri semanticUri = SemanticUri.parse("test1/test2");
        item = new Item(semanticUri);
        version = Version.builder()
                .from(NOW.plusDays(2))
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .content(content)
                .build();
        item.addVersion(version);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testOverlaps() {
        assertEquals(1, item.getVersions().size());
        assertNotNull(version.getItem());
        assertEquals(item, version.getItem());
    }

    @Test
    public void testIsComplete() {
        assertTrue(version.isComplete());
        //Group compGrp = new Group("test2", "test3");
        SemanticUri semanticUri = SemanticUri.parse("test2/test3");
        Item compItem = new Item(semanticUri);
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
        //Group compGrp = new Group("test2", "test3");
        SemanticUri semanticUri = SemanticUri.parse("test2/test3");
        Item compItem = new Item(semanticUri);
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
