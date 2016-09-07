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
package dk.kontentsu.cdn.exception;

/**
 * Unique identifies for each error that can occur.
 *
 * @author Jens Borch Christiansen
 */
public enum ErrorCode {

    UNKNOWN_ERROR,
    PERSISTENCE_ERROR,
    CONTENT_PROCESSING_ERROR,
    COMPOSITION_ERROR,
    VALIDATION_ERROR,
    REST_API_ERROR,
    NOT_MULTIPART_REQUEST_ERROR,
    CONTENT_UPLOAD_ERROR,
    EXTERNALIZATION_ERROR,
    NOT_FOUND_ERROR,
    MIME_TYPE_MISMATCH_ERROR,
    DB_CYPTO_ERROR;

    public String getErrorCode() {
        return name().toLowerCase().replace('_', '-');
    }

    @Override
    public String toString() {
        return getErrorCode();
    }
}
