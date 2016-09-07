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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import dk.kontentsu.util.DateTimeFormat.DateTimeFormatValidator;

/**
 *
 * @author Jens Borch Christiansen
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DateTimeFormatValidator.class})
@Documented
public @interface DateTimeFormat {

    String message() default "Invalid datetime string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Format value();

    /**
     * Validator for DateTimeFormatvalidation annotation.
     */
    class DateTimeFormatValidator implements ConstraintValidator<DateTimeFormat, String> {

        private DateTimeFormatter formatter;

        @Override
        public void initialize(final DateTimeFormat annotation) {
            formatter = annotation.value().getFormatter();
        }

        @Override
        public boolean isValid(final String datetime, final ConstraintValidatorContext context) {
            if (datetime == null) {
                //Use @NotNull for validating null
                return true;
            } else {
                try {
                    formatter.parse(datetime);
                    return true;
                } catch (DateTimeParseException e) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Invalid datetime string: "
                            + datetime + ". String must be formated using: " + formatter.toString())
                            .addConstraintViolation();
                    return false;
                }
            }
        }

    }

    /**
     * Enum of valid time formats for validation annotation.
     */
    enum Format {

        ISO_ZONED(DateTimeFormatter.ISO_ZONED_DATE_TIME),
        UTC(DateTimeFormatter.ISO_INSTANT);

        private final DateTimeFormatter formatter;

        Format(final DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        public DateTimeFormatter getFormatter() {
            return formatter;
        }

    }

}
