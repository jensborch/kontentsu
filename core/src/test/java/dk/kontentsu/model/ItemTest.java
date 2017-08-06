/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kontentsu.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dk.kontentsu.exception.ValidationException;

/**
 * Test for {@link Item}.
 *
 * @author Jens Borch Christiansen
 */
public class ItemTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private SemanticUriPath semanticUriPath;
    private Item item;

    @Before
    public void setUp() throws Exception {
        semanticUriPath = new SemanticUriPath("test1", "test2");
        item = new Item(new SemanticUri(semanticUriPath, "default"));
        item.addVersion(create(NOW, Interval.INFINITE));
    }

    private Version create(final ZonedDateTime from, final ZonedDateTime to) throws Exception {
        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));
        return Version.builder()
                .content(content)
                .from(from)
                .to(to)
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .build();
    }

    @Test
    public void testNoOverlaps() throws Exception {
        Version nooverlap = create(NOW.minusHours(1), NOW.minusNanos(1));
        item.addVersion(nooverlap);
        assertEquals(1, semanticUriPath.getItems().size());
        assertEquals(2, item.getVersions().size());
    }

    @Test
    public void testOverlapsEmpty() throws Exception {
        Version nooverlap = create(NOW.minusHours(1), NOW.minusNanos(1));
        assertFalse(item.overlaps(nooverlap));
    }

    @Test(expected = ValidationException.class)
    public void testVersionOverlap() throws Exception {
        Content content = new Content("Overlap".getBytes(), Charset.defaultCharset(), new MimeType("text", "plain"));

        Version version = Version.builder()
                .from(NOW.plusDays(2))
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .content(content)
                .build();
        item.addVersion(version);
    }

    @Test
    public void testGetVersions() throws Exception {
        Version overlap = create(NOW.minusHours(1), NOW.minusNanos(1));
        item.addVersion(overlap);
        List<Version> result = item.getVersions(new Interval(NOW.minusDays(1), NOW.plusDays(2)));
        assertEquals(2, result.size());
        result = item.getVersions(new Interval(NOW.minusNanos(2), NOW.plusMinutes(2)));
        assertEquals(2, result.size());
        result = item.getVersions(new Interval(NOW.minusDays(10), NOW.minusMinutes(1)));
        assertEquals(1, result.size());
        result = item.getVersions(new Interval(NOW.minusDays(10), NOW.minusMinutes(61)));
        assertEquals(0, result.size());
    }
}
