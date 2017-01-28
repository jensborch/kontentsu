package dk.kontentsu.api;

import dk.kontentsu.api.configuration.Config;
import dk.kontentsu.oauth2.module.AuthConfigProvider;
import dk.kontentsu.oauth2.module.Options;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Register OAuth2 JASPIC auth module, securing all API calls.
 *
 * @author Jens Borch Christiansen
 */
@WebListener
public class AuthModuleStarup implements ServletContextListener {

    @Inject
    private Config config;

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        AuthConfigProvider.register(new Options()
                .setSignatureKey(config.signatureKey()),
                event.getServletContext());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        //Do noothinng...
    }

}
