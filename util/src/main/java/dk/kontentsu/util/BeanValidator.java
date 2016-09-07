package dk.kontentsu.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;

/**
 * Bean validation utility methods.
 *
 * @author Jens Borch Christiansen
 */
public final class BeanValidator {

    private BeanValidator() {
    }

    /**
     * Validate method manually.
     */
    public static <T> Set<ConstraintViolation<T>> validate(final T obj, final String method, final Class<?>[] params, final Object[] values) {
        try {
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            ExecutableValidator validator = vf.getValidator().forExecutables();
            Method m = obj.getClass().getMethod(method, params);
            return validator.validateParameters(obj, m, values, Default.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ValidationException("Error validating method " + method + " on class " + obj.getClass().getName() + " with parmeters: " + Arrays.toString(values), ex);
        }
    }

}
