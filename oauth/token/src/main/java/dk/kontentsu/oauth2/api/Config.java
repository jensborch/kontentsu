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
package dk.kontentsu.oauth2.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * OAuth2 configuration.
 *
 * @author Jens Borch Christiansen
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_INTERFACE")
public interface Config extends org.aeonbits.owner.Config {

    /**
     * JWT signature key used in OAuth2 bearer token.
     *
     * @return a signature key string.
     */
    @Key("oauth2.jwt.signature.key")
    @DefaultValue("kontentsujwtkey")
    String signatureKey();

    /**
     * JWT token timeout in minutes.
     *
     * @return the timeout value.
     */
    @Key("oauth2.token.timeout")
    @DefaultValue("30")
    int timeout();

    /**
     * The name of the token issuer.
     *
     * @return the issuer.
     */
    @Key("oauth2.token.issuer")
    @DefaultValue("Kontentsu")
    String issuer();

}
