/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.api;

import dk.kontentsu.api.exceptionmappers.ApiExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.api.exceptionmappers.DefaultExceptionMapper;
import dk.kontentsu.api.exposure.CategoryExposure;
import dk.kontentsu.api.exposure.ExternalFileExposure;
import dk.kontentsu.api.exposure.HostExposure;
import dk.kontentsu.api.exposure.ItemExposure;
import dk.kontentsu.api.exposure.RootExposure;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * The application class for the REST exposure.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationPath(CdnApplication.API_ROOT)
public class CdnApplication extends Application {

    public static final String API_ROOT = "/api";

    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<Class<?>> classes = new HashSet<>();

    static {
        LOGGER.info("Loading CDN REST publiching application...");
    }

    public CdnApplication() {

        //Swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(RootExposure.class.getPackage().getName());
        beanConfig.setBasePath("cdn" + API_ROOT);
        beanConfig.setScan(true);
        classes.add(ApiListingResource.class);
        classes.add(SwaggerSerializers.class);

        //Exposures
        classes.add(ExternalFileExposure.class);
        classes.add(ItemExposure.class);
        classes.add(RootExposure.class);
        classes.add(CategoryExposure.class);
        classes.add(HostExposure.class);

        //Filters
        classes.add(CORSFilter.class);
        classes.add(DiagnosticFilter.class);

        //Exception mappers
        classes.add(DefaultExceptionMapper.class);
        classes.add(ApiExceptionMapper.class);
        classes.add(ConstraintViolationExceptionMapper.class);
        classes.add(ContainerExceptionMapper.class);

        //Jackson
        classes.add(JacksonFeature.class);
        classes.add(ObjectMapperProvider.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}
