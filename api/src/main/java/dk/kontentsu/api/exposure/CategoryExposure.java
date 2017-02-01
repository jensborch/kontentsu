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

import dk.kontentsu.api.model.CategoryRepresentation;
import dk.kontentsu.api.model.ErrorRepresentation;
import dk.kontentsu.model.Role;
import dk.kontentsu.model.Taxon;
import dk.kontentsu.model.Taxonomy;
import dk.kontentsu.repository.CategoryRepository;
import dk.kontentsu.repository.TaxonomyRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

    @Inject
    private CategoryRepository catRepo;

    @Inject
    private TaxonomyRepository taxRepo;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List of defined taxonomies")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Taxonomy link list", response = Link.class, responseContainer = "List")})
    public Response findAll() {
        List<Link> result = taxRepo.findAll()
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
        @ApiResponse(code = 200, message = "List of categories", response = Link.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "If taxonomy name can't be found", response = ErrorRepresentation.class)})
    public Response getTaxonomy(@PathParam("taxonomy") @NotNull final String name) {
        Taxonomy taxonomy = taxRepo.getByName(name);
        List<Link> result = catRepo.getByTaxonomy(taxonomy)
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
    @Path("{taxonomy}/{category:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a category definition")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A representation of an category", response = CategoryRepresentation.class),
        @ApiResponse(code = 404, message = "If category can't be found", response = ErrorRepresentation.class)})
    public Response getCategory(
            @PathParam("taxonomy") @NotNull final String taxonomyName,
            @PathParam("category") @NotNull final String categoryPath) {
        Taxonomy taxonomy = taxRepo.getByName(taxonomyName);
        CategoryRepresentation result = new CategoryRepresentation(catRepo.getByTaxonomy(taxonomy, categoryPath), uriInfo);
        return Response.ok().entity(result).build();
    }

    @POST
    @Path("{taxonomy}/{category:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a taxonomy and/or category",
            notes = "The firste element in the path is the name of the taxonomy. The operation is idempotent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Taxonomy and/or category has been created"),
        @ApiResponse(code = 404, message = "", response = ErrorRepresentation.class)})
    public Response createCategory(
            @PathParam("taxonomy") @NotNull final String taxonomyName,
            @PathParam("category") final String categoryPath) {
        Taxonomy taxonomy = taxRepo.findByName(taxonomyName).orElseGet(() -> {
            return taxRepo.save(new Taxonomy(taxonomyName));
        });
        if (categoryPath != null) {
            catRepo.findByTaxonomy(taxonomy, categoryPath)
                    .orElseGet(() -> catRepo.save(Taxon.parse(taxonomy, categoryPath)));
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().build()).build();
    }

    @DELETE
    @Path("{taxonomy}/{category:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a a taxonomy or category",
            notes = "The firste element in the path is the name of the taxonomy. The operation is idempotent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Taxonomy and/or category has been deleted"),
        @ApiResponse(code = 404, message = "", response = ErrorRepresentation.class)})
    public Response deleteCategory(
            @PathParam("taxonomy") @NotNull final String taxonomyName,
            @PathParam("category") final String categoryPath) {
        if (categoryPath == null) {
            taxRepo.findByName(taxonomyName).ifPresent(t -> taxRepo.delete(t.getUuid()));
        } else {
            taxRepo.findByName(taxonomyName)
                    .ifPresent(t -> catRepo
                            .findByTaxonomy(t, categoryPath)
                            .ifPresent(c -> catRepo.delete(c.getUuid())));
        }
        return Response.ok(uriInfo.getAbsolutePathBuilder().build()).build();
    }

}
