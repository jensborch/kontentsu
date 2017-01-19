package dk.kontentsu.oauth2.auth;

import java.util.Map;
import javax.security.auth.message.module.ServerAuthModule;

/**
 *
 * @author Jens Borch Christiansen
 */
public class AuthOptions {

    private final Map options;

    public AuthOptions(@SuppressWarnings("rawtypes") final Map options) {
        this.options = options;
    }

    public boolean isMandatory() {
        Object isMandatory = options.get("javax.security.auth.message.MessagePolicy.isMandatory");
        return isMandatory != null && isMandatory instanceof String && Boolean.valueOf((String) isMandatory);
    }

    public String getLoginContextName(final Class<? extends ServerAuthModule> clazz) {
        Object name = options.get("javax.security.auth.login.LoginContext");
        return (name != null && name instanceof String) ? (String) name : clazz.getName();
    }
}
