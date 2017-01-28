package dk.kontentsu.oauth2.module;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurations options for OAuth2 JASPIC
 *
 * @author Jens Borch Christiansen
 */
public class Options {

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
        return get(OAUTH2_JWT_SIGNATURE_KEY).getBytes(StandardCharsets.UTF_8);
    }

    public Options setSignatureKey(final String value) {
        set(OAUTH2_JWT_SIGNATURE_KEY, value);
        return this;
    }

    public Map asMap() {
        return Collections.unmodifiableMap(options);
    }

}
