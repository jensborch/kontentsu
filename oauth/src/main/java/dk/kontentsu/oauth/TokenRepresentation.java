package dk.kontentsu.oauth;

/**
 *
 * @author Jens Borch Christiansen
 */
public class TokenRepresentation {

    private final String accessToken;

    public TokenRepresentation(final String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
