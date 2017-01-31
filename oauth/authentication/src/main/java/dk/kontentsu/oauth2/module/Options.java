package dk.kontentsu.oauth2.module;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.message.AuthException;

/**
 * Configurations options for OAuth2 JASPIC server authentication module. This
 * the class encapsulates the raw map data type used in the JASPIC API, making
 * it easier to configure the authentication module. The class is mutable, and
 * setter methods can be chained providing a builder like API.
 *
 * @author Jens Borch Christiansen
 */
public class Options {

    public static final String IS_MANDATORY = "javax.security.auth.message.MessagsePolicy.isMandatory";
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
     * javax.security.auth.message.MessagsePolicy.isMandatory is equal to the
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
