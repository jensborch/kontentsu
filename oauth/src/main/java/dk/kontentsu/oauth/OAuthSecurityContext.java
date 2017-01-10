package dk.kontentsu.oauth;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Jens Borch Christiansen
 */
public class OAuthSecurityContext implements SecurityContext {

    private final User user;
    private final String scheme;

    public OAuthSecurityContext(final User user, final String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(final String role) {
        return user.getRoles().contains(role);
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }

}
