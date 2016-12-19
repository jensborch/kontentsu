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

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.kontentsu.api.model.ErrorRepresentation;
import dk.kontentsu.model.internal.Host;
import dk.kontentsu.repository.HostRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST resource for listing and manipulating host - i.e. destinations CDN items can be distributed to.
 *
 * @author Jens Borch Christiansen
 */
@Path("/hosts")
@Stateless
@Api(tags = {"hosts"})
public class HostExposure {

    @Inject
    private HostRepository repo;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Find all CDN host destinations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Host definotions", response = Link.class, responseContainer = "List")})
    public Response findAll() {
        List<Link> result = repo.findAll()
                .stream()
                .map(h -> Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                        .path(HostExposure.class)
                        .path(HostExposure.class, "get"))
                        .rel("host")
                        .title(h.getName())
                        .build(h.getName()))
                .collect(Collectors.toList());
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get CDN host destination")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Host definotion", response = Host.class),
        @ApiResponse(code = 404, message = "If UUID cant be found", response = ErrorRepresentation.class)})
    public Response get(@PathParam("name") final String name) {
        return Response.ok().entity(repo.getByName(name.trim())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create CDN host destination")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Host has been created"),
        @ApiResponse(code = 400, message = "If the payload is invalid", response = ErrorRepresentation.class)})
    public Response create(@Valid final Host host) {
        repo.save(host);
        URI uri = uriInfo.getAbsolutePathBuilder().build(Host.class);
        return Response.created(uri).build();
    }

    @DELETE
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete CDN host destination")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Host has been deleted"),
        @ApiResponse(code = 404, message = "If UUID cant be found", response = ErrorRepresentation.class)})
    public Response create(final String name) {
        repo.delete(name.trim());
        return Response.ok().build();
    }

}
