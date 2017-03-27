package dk.kontentsu.util.rs;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.ws.rs.core.CacheControl;

/**
 * JAX-RS CacheControl CDI producer that uses {@link CachControlConfig} to
 * configure cashing. Based on code from
 * <a href="https://abhirockzz.wordpress.com/2015/02/20/simplifying-jax-rs-caching-with-cdi/"></a>
 */
public class CacheControlProducer {

    @Produces
    public CacheControl create(InjectionPoint ip) {
        CachControlConfig config = ip.getAnnotated().getAnnotation(CachControlConfig.class);
        CacheControl cc = new CacheControl();
        if (config != null) {
            cc.setMaxAge(config.maxAge());
            cc.setMustRevalidate(config.mustRevalidate());
            cc.setNoCache(config.noCache());
            cc.setNoStore(config.noStore());
            cc.setNoTransform(config.noTransform());
            cc.setPrivate(config.isPrivate());
            cc.setProxyRevalidate(config.proxyRevalidate());
            cc.setSMaxAge(config.sMaxAge());
        }
        return cc;
    }

}
