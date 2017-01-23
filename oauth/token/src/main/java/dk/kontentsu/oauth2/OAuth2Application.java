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
package dk.kontentsu.oauth2;

import dk.kontentsu.oauth2.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.oauth2.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.oauth2.exceptionmappers.NotAuthorizedExceptionMapper;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The application class for the OAuth2 REST exposure.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationPath(OAuth2Application.API_ROOT)
public class OAuth2Application extends Application {

    public static final String API_ROOT = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Application.class);
    private final Set<Class<?>> classes = new HashSet<>();

    static {
        LOGGER.info("Loading OAuth application...");
    }

    public OAuth2Application() {
        classes.add(TokenExposure.class);
        classes.add(JacksonFeature.class);
        classes.add(NotAuthorizedExceptionMapper.class);
        classes.add(ContainerExceptionMapper.class);
        classes.add(ConstraintViolationExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}
