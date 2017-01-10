package dk.kontentsu.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Set;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author Jens Borch Christiansen
 */
public class OAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        String authHeader = context.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }

        String jwt = authHeader.substring("Bearer".length()).trim();

        Jws<Claims> claims = Jwts.parser().setSigningKey(getSignatureKey()).parseClaimsJws(jwt);

        String user = claims.getBody().getSubject();
        Set<String> groups = (Set<String>) claims.getBody().get("groups", Set.class);

        String scheme = context.getUriInfo().getRequestUri().getScheme();
        context.setSecurityContext(new OAuthSecurityContext(new User(user, groups), scheme));

    }

    private byte[] getSignatureKey() {
        return new byte[0];
    }

}
