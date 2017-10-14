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
package dk.kontentsu.api.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import dk.kontentsu.api.exposure.model.ErrorRepresentation;
import dk.kontentsu.exception.ErrorCode;
import dk.kontentsu.util.CauseFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception mapper for Throwable - will map to HTTP 500.
 *
 * @author Jens Borch Christiansen
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Response toResponse(final Throwable t) {
        LOGGER.warn("Unknown error in CDN application", t);

        String message = new CauseFinder(n -> n.getMessage() != null && !n.getMessage().isEmpty())
                .find(t)
                .map(Throwable::getMessage)
                .orElse("Unknown error");

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorRepresentation(ErrorCode.UNKNOWN_ERROR, message))
                .build();
    }

}
