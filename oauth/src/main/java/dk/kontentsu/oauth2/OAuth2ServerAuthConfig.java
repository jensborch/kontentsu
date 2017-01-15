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
public class OAuth2ServerAuthConfig implements ServerAuthConfig {

    private final String appContext;
    private final String messageLayer;
    private final CallbackHandler handler;

    public OAuth2ServerAuthConfig(
            final String appContext,
            final String messageLayer,
            final CallbackHandler handler) {
        this.appContext = appContext;
        this.messageLayer = messageLayer;
        this.handler = handler;
    }

    @Override
    public ServerAuthContext getAuthContext(final String authContextID,
            final Subject serviceSubject,
            final Map properties) throws AuthException {
        Map initOptions = new ConcurrentHashMap();
        if (properties != null) {
            initOptions.putAll(properties);
        }
        OAuth2Module module = new OAuth2Module();
        if (authContextID == null) {
            module.initialize(new MessagePolicy(new TargetPolicy[0], false),
                    new MessagePolicy(new TargetPolicy[0], false), handler, initOptions);
        } else {
            module.initialize(new MessagePolicy(new TargetPolicy[0], true),
                    new MessagePolicy(new TargetPolicy[0], true), handler, initOptions);
        }
        return module;
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(final MessageInfo messageInfo) throws IllegalArgumentException {
        Object isMandatory = messageInfo.getMap().get("javax.security.auth.message.MessagePolicy.isMandatory");
        if (isMandatory != null && isMandatory instanceof String && Boolean.valueOf((String) isMandatory)) {
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

}
