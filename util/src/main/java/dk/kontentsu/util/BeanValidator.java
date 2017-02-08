/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
            throw new ValidationException("Error validating method "
                    + method + " on class " + obj.getClass().getName() + " with parmeters: " + Arrays.toString(values), ex);
        }
    }
}
