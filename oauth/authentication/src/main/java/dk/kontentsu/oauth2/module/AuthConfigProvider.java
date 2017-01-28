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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.servlet.ServletContext;

/**
 * Configuration provider for the OAuth2 JASPIC authentication module.
 *
 * @author Jens Borch Christiansen
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_INTERFACE")
public class AuthConfigProvider implements javax.security.auth.message.config.AuthConfigProvider {

    private static final String LAYER = "HttpServlet";
    private static final String DESCRIPTION = "OAuth2 SAM authentication config provider";

    private final Options options;

    private AuthConfigProvider(final Options options) {
        this.options = options;
    }

    /**
     * Required constructor according to specifications.
     */
    public AuthConfigProvider(final Map<String, String> options, final AuthConfigFactory factory) {
        this(new Options(options));
        if (factory != null) {
            factory.registerConfigProvider(this, LAYER, null, DESCRIPTION);
        }
    }

    public static void register(final Options options, final ServletContext context) {
        AuthConfigFactory.getFactory().registerConfigProvider(new AuthConfigProvider(options),
                LAYER,
                getAppContextID(context),
                DESCRIPTION
        );
    }

    public static String getAppContextID(final ServletContext context) {
        return context.getVirtualServerName() + " " + context.getContextPath();
    }

    @Override
    public ClientAuthConfig getClientAuthConfig(final String layer,
            final String appContext,
            final CallbackHandler handler) throws AuthException, SecurityException {
        return new AuthConfig(appContext, layer, handler, options);
    }

    @Override
    public ServerAuthConfig getServerAuthConfig(final String layer,
            final String appContext,
            final CallbackHandler handler) throws AuthException, SecurityException {
        return new AuthConfig(appContext, layer, handler, options);
    }

    @Override
    public void refresh() {
        ///Do nothing...
    }

}
