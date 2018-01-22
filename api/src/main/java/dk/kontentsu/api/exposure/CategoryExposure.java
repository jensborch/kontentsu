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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.kontentsu.api.exposure.model.CategoryRepresentation;
import dk.kontentsu.api.exposure.model.ErrorRepresentation;
import dk.kontentsu.model.Role;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.TermRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST resource for listing and manipulating categories for items on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Path("/categories")
@Stateless
@DeclareRoles(Role.ADMIN)
@RolesAllowed(Role.ADMIN)
@Api(tags = {"categories"})
public class CategoryExposure {

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    private TermRepository termRepo;


    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List of defined taxonomies")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Taxonomy link list", response = Link.class, responseContainer = "List")})
    public Response findAll() {
        List<Link> result = termRepo.findAll()
                .stream()
                .map(t -> Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                        .path(CategoryExposure.class)
                        .path(CategoryExposure.class, "getTaxonomy"))
                        .rel("taxonomy")
                        .title(t.getName())
                        .build(t.getName()))
                .collect(Collectors.toList());
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{taxonomy}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get categories defined in a given taxonomy")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of root terms", response = Link.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "If taxonomy name can't be found", response = ErrorRepresentation.class)})
    public Response getTaxonomy(@PathParam("taxonomy") @NotNull @Size(min = 1) final String taxonomy) {
        Term term = termRepo.findAll().stream()
                .filter(t -> t.getName().equals(taxonomy)).findAny()
                .orElseThrow(() -> new NoResultException("Taxonomy '" + taxonomy + "' not found"));
        List<Link> result = term.getChildren()
                .stream()
                .map(c -> Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                        .path(CategoryExposure.class)
                        .path(CategoryExposure.class, "getCategory"))
                        .rel("category")
                        .title(c.toString())
                        .build(c.getTaxonomy().getName(), c.toString()))
                .collect(Collectors.toList());
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{taxonomy}/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a category definition")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A representation of an category", response = CategoryRepresentation.class),
        @ApiResponse(code = 404, message = "If category can't be found", response = ErrorRepresentation.class)})
    public Response getCategory(
            @PathParam("taxonomy") @NotNull final String taxonomy,
            @PathParam("path") @NotNull final String path) {
        Term term = termRepo.get(taxonomy +  Term.TAXONOMY_SEPARATOR + path);
        CategoryRepresentation result = new CategoryRepresentation(term, uriInfo);
        return Response.ok().entity(result).build();
    }

    @POST
    @Path("{taxonomy}/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a taxonomy and/or category",
            notes = "The first element in the path is the name of the taxonomy. The operation is idempotent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Taxonomy and/or category has been created"),
        @ApiResponse(code = 404, message = "", response = ErrorRepresentation.class)})
    public Response createCategory(
            @PathParam("taxonomy") @NotNull final String taxonomy,
            @PathParam("path") final String path) {
        termRepo.create(taxonomy +  Term.TAXONOMY_SEPARATOR + path);
        return Response.created(uriInfo.getAbsolutePathBuilder().build()).build();
    }

    @DELETE
    @Path("{taxonomy}/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a a taxonomy or category",
            notes = "The first element in the path is the name of the taxonomy. The operation is idempotent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Taxonomy and/or category has been deleted"),
        @ApiResponse(code = 404, message = "", response = ErrorRepresentation.class)})
    public Response deleteCategory(
            @PathParam("taxonomy") @NotNull final String taxonomy,
            @PathParam("path") final String path) {
        Term term = termRepo.get(taxonomy +  Term.TAXONOMY_SEPARATOR + path);
        termRepo.delete(term);
        return Response.ok(uriInfo.getAbsolutePathBuilder().build()).build();
    }

}
