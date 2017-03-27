package dk.kontentsu.util.rs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author Jens Borch Christiansen
 */
public class CacheFilter implements ContainerResponseFilter {

    private final CachControlConfig conf;
    private final Set<String> cacheControl;

    CacheFilter(final CachControlConfig conf) {
        this.conf = conf;
        this.cacheControl = new HashSet<>();
        if (conf.isPrivate()) {
            this.cacheControl.add("private");
        }
        if (conf.mustRevalidate()) {
            this.cacheControl.add("must-revalidate");
        }
        if (conf.noCache()) {
            this.cacheControl.add("no-cache");
        }
        if (conf.noStore()) {
            this.cacheControl.add("no-store");
        }
        if (conf.noTransform()) {
            this.cacheControl.add("no-transform");
        }
        if (conf.proxyRevalidate()) {
            this.cacheControl.add("proxy-revalidate");
        }
        this.cacheControl.add("max-age=" + this.conf.maxAge());
        this.cacheControl.add("s-max-age=" + this.conf.maxAge());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        conf.maxAge();

        response.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, "");

    }

}
