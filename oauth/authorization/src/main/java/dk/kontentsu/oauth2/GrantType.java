package dk.kontentsu.oauth2;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported OAuth2 grant types.
 */
public enum GrantType {

    PASSWORD("password");

    private final String type;

    GrantType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Map<String, GrantType> types() {
        return Arrays.stream(values()).collect(Collectors.toMap(GrantType::getType, t -> t));
    }

    public static Optional<GrantType> from(final String type) {
        return Optional.ofNullable(types().get(type));
    }

    public static boolean isSupported(final String type) {
        return from(type).isPresent();
    }

}
