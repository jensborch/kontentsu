package dk.kontentsu.api;

import dk.kontentsu.oauth2.auth.OAuth2Module;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
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
