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
package dk.kontentsu.oauth2;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.MessagePolicy.TargetPolicy;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

/**
 * Configuration for the OAuth2 Java EE server authentication module.
 *
 * @author Jens Borch Christiansen
 */
public class AuthConfig implements ServerAuthConfig {

    private final String appContext;
    private final String messageLayer;
    private final CallbackHandler handler;
    private final AuthConfig.Options options;

    public AuthConfig(
            final String appContext,
            final String messageLayer,
            final CallbackHandler handler,
            final Options options) {
        this.options = options;
        this.appContext = appContext;
        this.messageLayer = messageLayer;
        this.handler = handler;
    }

    @Override
    public ServerAuthContext getAuthContext(
            final String authContextID,
            final Subject serviceSubject,
            @SuppressWarnings("rawtypes") final Map properties) throws AuthException {
        options.augment(properties);
        AuthModule module = new AuthModule();
        if (authContextID == null) {
            module.initialize(new MessagePolicy(new TargetPolicy[0], false),
                    new MessagePolicy(new TargetPolicy[0], false), handler, options.asMap());
        } else {
            module.initialize(new MessagePolicy(new TargetPolicy[0], true),
                    new MessagePolicy(new TargetPolicy[0], true), handler, options.asMap());
        }
        return module;
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(final MessageInfo messageInfo) throws IllegalArgumentException {
        if (new Options(messageInfo.getMap()).isMandatory()) {
            return messageInfo.toString();
        }
        return null;
    }

    @Override
    public String getMessageLayer() {
        return messageLayer;
    }

    @Override
    public boolean isProtected() {
        //TODO: Check spec
        return false;
    }

    @Override
    public void refresh() {
        ///Do nothng...
    }

    /**
     * Configurations options.
     */
    public static class Options {

        public static final String IS_MANDATORY = "javax.security.auth.message.MessagsePolicy.isMandatory";
        public static final String OAUTH2_JWT_SIGNATURE_KEY = "oauth2.jwt.signature.key";

        private final Map<String, String> options;

        public Options() {
            this.options = new ConcurrentHashMap<>(2);
        }

        @SuppressWarnings("unchecked")
        Options(final Map options) {
            this();
            this.options.putAll(options);
        }

        @SuppressWarnings("unchecked")
        public Options augment(final Map options) {
            this.options.putAll(options);
            return this;
        }

        private String get(final String key) {
            return options.get(key);
        }

        private void set(final String key, final String option) {
            options.put(key, option);
        }

        public boolean isMandatory() {
            return Boolean.valueOf(options.get(IS_MANDATORY));
        }

        public byte[] getSignatureKey() {
            return get(OAUTH2_JWT_SIGNATURE_KEY).getBytes();
        }

        public Options setSignatureKey(final String value) {
            set(OAUTH2_JWT_SIGNATURE_KEY, value);
            return this;
        }

        public Map asMap() {
            return Collections.unmodifiableMap(options);
        }

    }

}
