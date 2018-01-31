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
package dk.kontentsu.jpa;

import dk.kontentsu.exception.ApplicationException;
import dk.kontentsu.exception.ErrorCode;

/**
 * Thrown when an error occurs encrypting or decrypting database column.
 *
 * @author Jens Borch Christiansen
 */
public class CryptoException extends ApplicationException {

    private static final long serialVersionUID = -5844275330766000853L;

    public CryptoException(final String message) {
        super(message);
    }

    public CryptoException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.DB_CRYPTO_ERROR;
    }

}
