package dk.kontentsu.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link Interval}
 *
 * @author Jens Borch Christiansen
 */
public class IntervalTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private Interval max;
    private Interval nowMax;
    private Interval nowPlus2days;
    private Interval nowPlus4days;
    private Interval plus4daysPlus8days;

    @Before
    public void setUp() {
        max = new Interval(ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault()), ZonedDateTime.of(LocalDateTime.MAX, ZoneId.systemDefault()));
        nowPlus2days = new Interval(NOW, NOW.plusDays(2));
        nowPlus4days = new Interval(NOW, NOW.plusDays(4));
        plus4daysPlus8days = new Interval(NOW.plusDays(4), NOW.plusDays(8));
        nowMax = new Interval(NOW, null);
    }

    @Test
    public void testSorting() {
        assertThat(nowPlus2days.compareTo(nowPlus4days), lessThan(0));
        assertThat(plus4daysPlus8days.compareTo(nowPlus2days), greaterThan(0));
    }

    @Test
    public void testOverlaps() {
        assertTrue(max.overlaps(nowPlus4days));
        assertTrue(max.overlaps(plus4daysPlus8days));
        assertTrue(nowPlus2days.overlaps(nowPlus4days));
        assertFalse(nowPlus4days.overlaps(plus4daysPlus8days));
        assertFalse(plus4daysPlus8days.overlaps(nowPlus4days));
        assertFalse(nowPlus2days.overlaps(plus4daysPlus8days));
        assertTrue(nowMax.overlaps(plus4daysPlus8days));
    }

    @Test
    public void testInvalid() {
        Interval invalid = new Interval(NOW, NOW);
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Interval>> errors = validator.validate(invalid, Default.class);
        assertEquals(1, errors.size());
        assertTrue(errors.stream().findFirst().get().getMessage().startsWith("Datetime 'from="));
    }

    @Test
    public void testEquals() {
        assertFalse(new Interval(NOW, NOW).equals(null));
        assertFalse(new Interval(NOW, NOW).equals(new Object()));
        assertTrue(new Interval(NOW, NOW).equals(new Interval(NOW, NOW)));
        assertFalse(new Interval(NOW.plusDays(1), NOW).equals(new Interval(NOW, NOW)));
    }

    @Test
    public void testIntersection() {
        assertEquals(new Interval(NOW, NOW.plusDays(2)), nowPlus2days.intersection(nowPlus4days).get());
        assertFalse(nowPlus2days.intersection(plus4daysPlus8days).isPresent());
    }

    @Test
    public void testDisjunctiveUnion() {
        Set<Interval> results = nowPlus2days.disjunctiveUnion(nowPlus4days);
        assertEquals(1, results.size());
        assertTrue(results.contains(new Interval(NOW.plusDays(2), NOW.plusDays(4))));
        results = nowPlus4days.disjunctiveUnion(new Interval(NOW.plusDays(1), NOW.plusDays(3)));
        assertEquals(2, results.size());
        assertTrue(results.contains(new Interval(NOW, NOW.plusDays(1))));
        assertTrue(results.contains(new Interval(NOW.plusDays(3), NOW.plusDays(4))));
    }

}
