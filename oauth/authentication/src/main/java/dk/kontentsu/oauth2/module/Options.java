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
package dk.kontentsu.oauth2.module;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;

/**
 * Configurations options for OAuth2 JASPIC server authentication module. This
 * the class encapsulates the raw map data type used in the JASPIC API, making
 * it easier to configure the authentication module. The class is mutable, and
 * setter methods can be chained providing a builder like API.
 *
 * @author Jens Borch Christiansen
 */
public class Options {

    public static final String IS_MANDATORY = "javax.security.auth.message.MessagePolicy.isMandatory";
    public static final String OAUTH2_JWT_SIGNATURE_KEY = "oauth2.jwt.signature.key";

    private final Map<String, String> values;

    public Options() {
        this.values = new ConcurrentHashMap<>(2);
    }

    @SuppressWarnings("unchecked")
    Options(final Map options) {
        this();
        this.values.putAll(options);
    }

    static Options parse(final MessageInfo messageInfo) {
        return new Options(messageInfo.getMap());
    }

    @SuppressWarnings("unchecked")
    public Options augment(final Map options) {
        this.values.putAll(options);
        return this;
    }

    private String get(final String key) {
        return values.get(key);
    }

    private void set(final String key, final String option) {
        values.put(key, option);
    }

    /**
     * Returns true if message policy is mandatory - i.e. if the key
     * javax.security.auth.message.MessagePolicy.isMandatory is equal to the
     * string true.
     */
    public boolean isMandatory() {
        return Boolean.valueOf(values.get(IS_MANDATORY));
    }

    /**
     * Returns the JWT signature key used for the OAuth2 token.
     *
     * @return signature key
     * @throws AuthException if signature key is null or empty
     */
    public byte[] getSignatureKey() throws AuthException {
        String key = get(OAUTH2_JWT_SIGNATURE_KEY);
        if (key == null || key.isEmpty()) {
            throw new AuthException("JWT signature key is not defined");
        }
        return get(OAUTH2_JWT_SIGNATURE_KEY).getBytes(StandardCharsets.UTF_8);

    }

    public Options setSignatureKey(final String value) {
        set(OAUTH2_JWT_SIGNATURE_KEY, value);
        return this;
    }

    public Map asMap() {
        return Collections.unmodifiableMap(values);
    }

}
