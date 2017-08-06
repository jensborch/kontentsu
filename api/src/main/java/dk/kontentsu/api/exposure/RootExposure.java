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
package dk.kontentsu.api.exposure;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.kontentsu.api.KontentsuApplication;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Root exposure of the CDN Publishing Services listing all available resources. Exposes files on the CDN similar to a HTTP server.
 *
 * @author Jens Borch Christiansen
 */
@Path("/")
@Stateless
@PermitAll
@SwaggerDefinition(
        info = @Info(
                title = "Kontentsu",
                description = "Kontentsu REST API",
                version = "V1.0.0",
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT")),
        basePath = KontentsuApplication.API_ROOT,
        schemes = {SwaggerDefinition.Scheme.DEFAULT},
        tags = {
            @Tag(name = "root", description = "Kontentsu API root resource"),
            @Tag(name = "hosts", description = "CDN publishing destination hosts"),
            @Tag(name = "categories", description = "Categories for items in Kontentsu"),
            @Tag(name = "items", description = "Internal API for manipulation items in Kontentsu"),
            @Tag(name = "files", description = "External exposure of the files that can be published to a CDN host - similarly to a HTTP server")
        })
@Api(tags = {"root"})
public class RootExposure {

    private static final int CACHE_MAX_AGE = 60 * 10;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List of CDN publishing service resources")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Resource list", response = Link.class, responseContainer = "List")})
    public Response get() {
        List<Link> links = new ArrayList<>();
        links.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(CategoryExposure.class))
                .rel("categories")
                .title("Categories for items on the CDN")
                .build());

        links.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(ExternalFileExposure.class))
                .rel("externalFiles")
                .title("Published external files")
                .build());

        links.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(HostExposure.class))
                .rel("hosts")
                .title("Destination hosts for CDN files")
                .build());

        links.add(Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(ItemExposure.class))
                .rel("items")
                .title("Uploaded CDN items")
                .build());

        CacheControl cc = new CacheControl();
        cc.setMaxAge(CACHE_MAX_AGE);

        return Response
                .ok()
                .cacheControl(cc)
                .entity(links)
                .build();
    }

}
