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

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.MessagePolicy.TargetPolicy;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;

/**
 * Configuration for the OAuth2 JASPIC server authentication module.
 *
 * @author Jens Borch Christiansen
 */
public class AuthConfig implements ServerAuthConfig, ClientAuthConfig {

    private final CallbackHandler handler;
    private final String appContext;
    private final String messageLayer;
    private final Options options;

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
    public AuthModule getAuthContext(
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

    /**
     * Will as defined by the JASPIC specifications return a authentication
     * context identifier if
     * <code>javax.security.auth.message.MessagsePolicy.isMandatory</code> is
     * true, otherwise null will be returned.
     *
     * @param messageInfo A contextual Object that encapsulates the client
     * request and server response objects.
     * @return an identifier or null
     * @throws IllegalArgumentException if the type of the message objects
     * incorporated in messageInfo are not compatible with the message type
     * supported
     */
    @Override
    public String getAuthContextID(final MessageInfo messageInfo) throws IllegalArgumentException {
        if (Options.parse(messageInfo).isMandatory()) {
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
        return false;
    }

    @Override
    public void refresh() {
        ///Do nothng...
    }
}
