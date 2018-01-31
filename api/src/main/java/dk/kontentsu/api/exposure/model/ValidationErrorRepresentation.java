package dk.kontentsu.api.exposure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.validation.ConstraintViolationException;

import dk.kontentsu.exception.ErrorCode;
import io.swagger.annotations.ApiModelProperty;

/**
 * Error response representation for a bean validation error.
 *
 * @author Jens Borch Christiansen
 */
public class ValidationErrorRepresentation extends ErrorRepresentation {

    @ApiModelProperty(value = "List of validation errors", required = true)
    private final List<Violation> violations = new ArrayList<>();

    public ValidationErrorRepresentation(final ErrorCode code, final ConstraintViolationException e) {
        super(code, e.getMessage());
        e.getConstraintViolations().forEach(v ->
                violations.add(new Violation(v.getMessage(), Objects.toString(v.getPropertyPath())))
        );
    }

    public List<Violation> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    /**
     * Representation of a single bean validation error.
     */
    public static class Violation {

        @ApiModelProperty(value = "Error message", required = true)
        private final String msg;

        @ApiModelProperty(value = "Path to invalid field", required = true)
        private final String path;

        public Violation(final String msg, final String path) {
            this.msg = msg;
            this.path = path;
        }

        public String getMsg() {
            return msg;
        }

        public String getPath() {
            return path;
        }

    }

}
