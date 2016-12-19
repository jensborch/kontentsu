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
package dk.kontentsu.cdn.api.exceptionmappers;

import java.util.Optional;
import java.util.function.Predicate;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.api.model.ErrorRepresentation;
import dk.kontentsu.cdn.api.model.ValidationErrorRepresentation;
import dk.kontentsu.exception.ErrorCode;

/**
 * Exception mapper for Throwable. If Throwable is an EJBException, a nested NoResultException will be mapped to at HTTP 404 and a nested ConstraintViolationException will be
 * mapped to 400.
 *
 * All other exceptions will be mapped to HTTP 500.
 *
 * @author Jens Borch Christiansen
 */
@Provider
public class ContainerExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable t) {
        if (t instanceof EJBException) {
            Optional<Throwable> ex = new CauseFinder(n -> n instanceof NoResultException).findCause(t);
            if (ex.isPresent()) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new ErrorRepresentation(ErrorCode.NOT_FOUND_ERROR, ex.get().getMessage()))
                        .build();
            }
            ex = new CauseFinder(n -> n instanceof ConstraintViolationException).findCause(t);
            if (ex.isPresent()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ValidationErrorRepresentation(ErrorCode.VALIDATION_ERROR, (ConstraintViolationException) ex.get()))
                        .build();
            }
        }
        LOGGER.warn("Unknown container error in CDN application", t);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorRepresentation(ErrorCode.UNKNOWN_ERROR, t.getMessage()))
                .build();
    }

    /**
     * Iterates through all causes to find exception matching predicate.
     */
    public static class CauseFinder {

        private final Predicate<Throwable> predicate;

        public CauseFinder(final Predicate<Throwable> predicate) {
            this.predicate = predicate;
        }

        Optional<Throwable> findCause(final Throwable t) {
            Throwable cause = t.getCause();
            return cause == null ? Optional.empty()
                    : predicate.test(cause) ? Optional.of(cause) : findCause(cause);
        }
    }

}
