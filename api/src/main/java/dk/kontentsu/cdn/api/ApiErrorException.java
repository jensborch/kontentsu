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
package dk.kontentsu.cdn.api;

import javax.ws.rs.core.Response;

import dk.kontentsu.cdn.exception.ApplicationException;
import dk.kontentsu.cdn.exception.ErrorCode;

/**
 *
 * @author Jens Borch Christiansen
 */
public class ApiErrorException extends ApplicationException {

    private static final long serialVersionUID = -2905672611361359319L;

    public ApiErrorException(final String message) {
        super(message);
    }

    public ApiErrorException(final String message, final Exception ex) {
        super(message, ex);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.REST_API_ERROR;
    }

    public Response.Status getHttpErrorCode() {
        return Response.Status.BAD_REQUEST;
    }

}
