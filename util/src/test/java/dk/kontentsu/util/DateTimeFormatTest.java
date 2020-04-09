package dk.kontentsu.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link DateTimeFormat}
 */
@ExtendWith(MockitoExtension.class)
public class DateTimeFormatTest {

    @Mock
    private DateTimeFormat annotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Test
    public void testValidateUTC() {
        when(annotation.value()).thenReturn(DateTimeFormat.Format.UTC);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        DateTimeFormat.Validator validator = new DateTimeFormat.Validator();
        validator.initialize(annotation);
        assertFalse(validator.isValid("2016-01-02 12:00", context));
        assertTrue(validator.isValid("2016-01-03T10:15:30Z", context));
        verify(builder, times(1)).addConstraintViolation();
    }
}
