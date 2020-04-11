/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kontentsu.model;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dk.kontentsu.exception.ValidationException;

/**
 * Test for {@link Item}.
 *
 * @author Jens Borch Christiansen
 */
public class ItemTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private Term path;
    private Item item;

    @BeforeEach
    public void setUp() {
        path = new Term().append("test1").append("test2");
        item = new Item(path, "default", new MimeType("text", "plain"));
        item.addVersion(create(NOW, Interval.INFINITE));
    }

    private Version create(final ZonedDateTime from, final ZonedDateTime to) {
        Content content = new Content("This is a test".getBytes(), Charset.defaultCharset());
        return Version.builder()
                .content(content)
                .from(from)
                .to(to)
                .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                .build();
    }

    @Test
    public void testSorting() {
        assertThat(create(NOW, NOW.plusMinutes(5)).compareTo(create(NOW.plusMinutes(5), NOW.plusMinutes(10))), lessThan(0));
        assertThat(create(NOW.minusMinutes(10), NOW.minusMinutes(5)).compareTo(create(NOW.minusDays(10), NOW.minusDays(5))), greaterThan(0));
    }

    @Test
    public void testNoOverlaps() {
        Version nooverlap = create(NOW.minusHours(1), NOW.minusNanos(1));
        item.addVersion(nooverlap);
        assertEquals(1, path.getItems().size());
        assertEquals(2, item.getVersions().size());
    }

    @Test
    public void testOverlapsEmpty() {
        Version nooverlap = create(NOW.minusHours(1), NOW.minusNanos(1));
        assertFalse(item.overlaps(nooverlap));
    }

    @Test
    public void testVersionOverlap() {
        ValidationException e = assertThrows(ValidationException.class, () -> {
            Content content = new Content("Overlap".getBytes(), Charset.defaultCharset());

            Version version = Version.builder().from(NOW.plusDays(2))
                    .metadata(new Metadata.Key(MetadataType.PAGE, "key"), new Metadata("This is metadata"))
                    .content(content).build();
            item.addVersion(version);
        });
        assertThat(e.getMessage(), startsWith("Version with interva"));
    }

    @Test
    public void testGetVersions() {
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
