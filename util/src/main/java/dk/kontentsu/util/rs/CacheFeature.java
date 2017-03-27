package dk.kontentsu.util.rs;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 *
 * @author Jens Borch Christiansen
 */
public class CacheFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        CachControlConfig conf = resourceInfo.getResourceMethod().getAnnotation(CachControlConfig.class);
        if (conf != null) {
            context.register(new CacheFilter(conf));
        }
    }

}
