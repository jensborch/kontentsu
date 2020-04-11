package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.net.URI;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link Host} class.
 *
 * @author Jens Borch Christiansen
 */
public class HostTest {

    @Test
    public void testInvalidPath() throws Exception {
        Host invalid = new Host("name", "desc", new URI("ftp://test"), "test \t test");
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Host>> errors = validator.validate(invalid, Default.class);
        assertEquals(1, errors.size());
        assertThat(errors.stream().findFirst().get().getMessage(), startsWith("must match"));
    }

    @Test
    public void testValidPath() throws Exception {
        Host invalid = new Host("name", "desc", new URI("ftp://test"), "path/test   /  \\");
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Host>> errors = validator.validate(invalid, Default.class);
        assertEquals(0, errors.size());
    }

}
