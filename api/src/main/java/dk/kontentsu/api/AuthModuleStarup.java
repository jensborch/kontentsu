package dk.kontentsu.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import dk.kontentsu.oauth2.OAuth2Module;

/**
 * Register OAuth2 server authorization module, securing all API calls.
 *
 * @author Jens Borch Christiansen
 */
@WebListener
public class AuthModuleStarup implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        OAuth2Module.register(event.getServletContext());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        //Do noothinng...
    }

}
