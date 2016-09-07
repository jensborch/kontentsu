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
package dk.kontentsu.cdn.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import dk.kontentsu.cdn.model.MimeTypeString.MimeTypeStringValidator;

/**
 * Bean validation constraint for a mimetype string.
 *
 * @author Jens Borch Christiansen
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {MimeTypeStringValidator.class})
@Documented
public @interface MimeTypeString {

    String message() default "Invalid mimetype string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean parameters() default true;

    /**
     * Validator for mimetype annotation.
     */
    class MimeTypeStringValidator implements ConstraintValidator<MimeTypeString, String> {

        private boolean parameters;

        @Override
        public void initialize(final MimeTypeString annotation) {
            parameters = annotation.parameters();
        }

        @Override
        public boolean isValid(final String mimetype, final ConstraintValidatorContext context) {
            if (mimetype == null) {
                //Use @NotNull for validating null
                return true;
            } else {
                try {
                    MimeType m = MimeType.parse(mimetype);
                    return (parameters) ? true : m.getParams().isEmpty();
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

    }

}
