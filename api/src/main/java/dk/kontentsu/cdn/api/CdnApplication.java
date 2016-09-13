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
package dk.kontentsu.cdn.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.api.exceptionmappers.ApiExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.cdn.api.exposure.CategoryExposure;
import dk.kontentsu.cdn.api.exposure.ExternalFileExposure;
import dk.kontentsu.cdn.api.exposure.HostExposure;
import dk.kontentsu.cdn.api.exposure.ItemExposure;
import dk.kontentsu.cdn.api.exposure.RootExposure;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

/**
 * The application class for the REST exposure.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationPath(CdnApplication.API_ROOT)
public class CdnApplication extends Application {

    public static final String API_ROOT = "/api";

    private static final Logger LOGGER = LoggerFactory.getLogger(CdnApplication.class);
    private final Set<Class<?>> classes = new HashSet<>();

    static {
        LOGGER.info("Loading CDN REST publiching application...");
    }

    public CdnApplication() {

        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(RootExposure.class.getPackage().getName());
        beanConfig.setBasePath("cdn" + API_ROOT);
        beanConfig.setScan(true);

        classes.add(JacksonFeature.class);

        classes.add(ExternalFileExposure.class);
        classes.add(ItemExposure.class);
        classes.add(RootExposure.class);
        classes.add(CategoryExposure.class);
        classes.add(HostExposure.class);

        classes.add(ObjectMapperProvider.class);
        classes.add(CORSFilter.class);

        classes.add(ApiListingResource.class);
        classes.add(SwaggerSerializers.class);

        classes.add(ApiExceptionMapper.class);
        classes.add(ConstraintViolationExceptionMapper.class);
        classes.add(ContainerExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}
