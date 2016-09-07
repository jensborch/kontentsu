
package dk.kontentsu.cdn.api.exposure;

import java.util.Map;

import javax.ws.rs.core.Link;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;

/**
 *
 * @author Jens Borch Christiansen
 */
@SwaggerDefinition
public class SwaggerLinkDefinition implements ReaderListener {

    @Override
    public void afterScan(final Reader reader, final Swagger swagger) {
        Map<String, Model> def = swagger.getDefinitions();
        Model model = def.get(Link.class.getSimpleName());
        Map<String, Property> props = model.getProperties();
        props.remove("type");
        props.remove("params");
        props.remove("uriBuilder");
        props.remove("rels");
    }

    @Override
    public void beforeScan(final Reader reader, final Swagger swagger) {
        //DO nothing...
    }

}
