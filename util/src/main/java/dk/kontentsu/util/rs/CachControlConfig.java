package dk.kontentsu.util.rs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration annotation for injected {@link javax.ws.rs.core.CacheControl}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface CachControlConfig {

    public boolean isPrivate() default true;

    public boolean noCache() default false;

    public boolean noStore() default false;

    public boolean noTransform() default true;

    public boolean mustRevalidate() default true;

    public boolean proxyRevalidate() default false;

    public int maxAge() default 0;

    public int sMaxAge() default 0;

}
