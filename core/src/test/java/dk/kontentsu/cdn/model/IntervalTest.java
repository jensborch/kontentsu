package dk.kontentsu.cdn.model;

import dk.kontentsu.cdn.model.Interval;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
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

    @After
    public void tearDown() {
    }

    @Test
    public void testOverlaps() {
        assertTrue(max.overlaps(nowPlus4days));
        assertTrue(max.overlaps(plus4daysPlus8days));
        assertTrue(nowPlus2days.overlaps(nowPlus4days));
        assertFalse(nowPlus4days.overlaps(plus4daysPlus8days));
        assertFalse(nowPlus2days.overlaps(plus4daysPlus8days));
        assertTrue(nowMax.overlaps(plus4daysPlus8days));
    }

    @Test
    public void testInvalid() throws Exception {
        Interval invalid = new Interval(NOW, NOW);
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Interval>> errors = validator.validate(invalid, Default.class);
        assertEquals(1, errors.size());
        assertTrue(errors.stream().findFirst().get().getMessage().startsWith("Datetime 'from="));
    }

    @Test
    public void testEquals() throws Exception {
        assertFalse(new Interval(NOW, NOW).equals(null));
        assertFalse(new Interval(NOW, NOW).equals(new Object()));
        assertTrue(new Interval(NOW, NOW).equals(new Interval(NOW, NOW)));
        assertFalse(new Interval(NOW.plusDays(1), NOW).equals(new Interval(NOW, NOW)));
    }

}
