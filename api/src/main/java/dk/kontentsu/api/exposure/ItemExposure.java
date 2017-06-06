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

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.api.ApiErrorException;
import dk.kontentsu.api.configuration.Config;
import dk.kontentsu.api.exposure.mappers.MultipartUploadItemMapper;
import dk.kontentsu.api.exposure.mappers.UploadItemMapper;
import dk.kontentsu.api.exposure.model.ErrorRepresentation;
import dk.kontentsu.api.exposure.model.ItemRepresentation;
import dk.kontentsu.api.exposure.model.MultipartUploadItemRepresentation;
import dk.kontentsu.api.exposure.model.UploadItemRepresentation;
import dk.kontentsu.api.exposure.model.VersionLinkRepresentation;
import dk.kontentsu.api.exposure.model.VersionRepresentation;
import dk.kontentsu.exception.ValidationException;
import dk.kontentsu.jackson.ObjectMapperFactory;
import dk.kontentsu.model.Item.Criteria;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Role;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.upload.UploadItem;
import dk.kontentsu.upload.UploadService;
import dk.kontentsu.util.rs.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

/**
 * REST resource for listing and manipulating items on the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Path("/items")
@Stateless
@Api(tags = {"items"})
@DeclareRoles(Role.ADMIN)
@RolesAllowed(Role.ADMIN)
public class ItemExposure {

    private static final String UPLOAD_ITEM_METADATA_FORM_FIELD = "uploaditem";

    @Inject
    private Config config;

    @Inject
    private UploadService service;

    @Inject
    private ItemRepository repo;

    @Context
    private UriInfo uriInfo;

    private FileItemFactory itemFactory;

    @PostConstruct
    protected void createDiskFileItemFactory() {
        itemFactory = new DiskFileItemFactory(config.write2diskSizeThreshold(), null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 30)
    public Response list() {
        List<ItemRepresentation> result = repo
                .find(Criteria.create(true))
                .stream()
                .map(i -> new ItemRepresentation(i, uriInfo))
                .collect(Collectors.toList());
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 300)
    public Response get(@PathParam("id") final String id) {
        return Response.ok().entity(new ItemRepresentation(repo.get(UUID.fromString(id)), uriInfo)).build();
    }

    @GET
    @Path("{id}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 30)
    public Response getVersions(@PathParam("id") final String id) {
        List<VersionLinkRepresentation> result = repo.get(UUID.fromString(id))
                .getVersions()
                .stream()
                .map(v -> new VersionLinkRepresentation(v, uriInfo))
                .collect(Collectors.toList());
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{item}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Cache(maxAge = 300)
    public Response getVersion(@PathParam("item") final String item, @PathParam("version") final String version) {
        Optional<VersionRepresentation> result = repo.get(UUID.fromString(item))
                .getVersions()
                .stream()
                .filter(v -> v.getUuid().equals(UUID.fromString(version)))
                .map(v -> new VersionRepresentation(v, uriInfo))
                .findAny();
        if (!result.isPresent()) {
            throw new ValidationException("Invalid UUID for version");
        }
        return Response.ok().entity(result.get()).build();
    }

    @DELETE
    @Path("{item}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("item") final String item, @PathParam("version") final String version) {
        repo.find(UUID.fromString(item))
                .ifPresent(i -> i.getVersions()
                .stream()
                .filter(v -> v.getUuid().equals(UUID.fromString(version)))
                .findAny()
                .ifPresent(v -> v.delete()));
        return Response.ok().build();
    }

    @DELETE
    @Path("{item}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("item") final String item) {
        repo.find(UUID.fromString(item))
                .ifPresent(i -> i.getVersions()
                .stream()
                .forEach(v -> v.delete()));
        return Response.ok().build();
    }

    /**
     * Upload and overwrite content for existing item using JSON with an URL.
     */
    @PUT
    @Path("{item}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.NEVER)

    @ApiOperation(value = "Overwrite existing content on the CDN using a data from a URL",
            notes = "Encoding must be specified for textual content")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Content has been uploaded"),
        @ApiResponse(code = 400, message = "If the payload is invalid", response = ErrorRepresentation.class)})
    public Response overwrite(@PathParam("item") final String item, @Valid final UploadItemRepresentation uploadItemRepresentation) {
        service.overwrite(UUID.fromString(item), new UploadItemMapper().apply(uploadItemRepresentation));
        URI uri = uriInfo.getAbsolutePathBuilder().build(UploadItem.class);
        return Response.created(uri).build();
    }

    /**
     * Upload content to CDN using JSON and URL.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.NEVER)
    @ApiOperation(value = "Upload content to the CDN using a data from a URL",
            notes = "Encoding must be specified for textual content")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Content has been uploaded"),
        @ApiResponse(code = 400, message = "If the payload is invalid", response = ErrorRepresentation.class)})
    public Response uploade(@Valid final UploadItemRepresentation uploadItemRepresentation) {
        service.upload(new UploadItemMapper().apply(uploadItemRepresentation));
        URI uri = uriInfo.getAbsolutePathBuilder().build(UploadItem.class);
        return Response.created(uri).build();
    }

    @PUT
    @Path("{item}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.NEVER)

    @ApiOperation(value = "Overwrite existing content on the CDN using multipart attachment",
            notes = "Encoding must be specified for textual content", hidden = true)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "uploaditem",
                value = "Multipart upload JSON",
                required = true,
                dataType = "dk.kontentsu.cdn.api.model.MultipartUploadItemRepresentation",
                paramType = "form"),
        @ApiImplicitParam(name = "data",
                value = "Multipart attachment to upload to CDN, name must match contentRef in uploaditem JSON",
                required = true,
                dataType = "java.io.File",
                paramType = "body")})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Content has been uploaded"),
        @ApiResponse(code = 400, message = "If the payload is invalid", response = ErrorRepresentation.class)})
    public Response overwrite(@PathParam("item") final String item, @Context final HttpServletRequest request) {
        return processMultipartRequest(request, u -> {
            UUID id = UUID.fromString(item);
            service.overwrite(id, u);
            return id;
        });
    }

    /**
     * Upload content to CDN using multipart request. Content should be added as
     * an attachment.
     *
     * <em>Note:</em> Swagger do not support operation overloading even with
     * different content types, so no documentation is created for this method.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.NEVER)

    @ApiOperation(value = "Upload content to the CDN using multipart attachment",
            notes = "Encoding must be specified for textual content", hidden = true)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "uploaditem",
                value = "Multipart upload JSON",
                required = true,
                dataType = "dk.kontentsu.cdn.api.model.MultipartUploadItemRepresentation",
                paramType = "form"),
        @ApiImplicitParam(name = "data",
                value = "Multipart attachment to upload to CDN, name must match contentRef in uploaditem JSON",
                required = true,
                dataType = "java.io.File",
                paramType = "body")})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Content has been uploaded"),
        @ApiResponse(code = 400, message = "If the payload is invalid", response = ErrorRepresentation.class)})
    public Response uploade(@Context final HttpServletRequest request) {
        return processMultipartRequest(request, u -> service.upload(u));
    }

    public Response processMultipartRequest(final HttpServletRequest request, final Function<UploadItem, UUID> strategy) {
        try {
            if (isMultipartContent(request)) {
                ServletFileUpload upload = new ServletFileUpload(itemFactory);
                UploadItem item = processMultipartItems(upload.parseRequest(request));
                return getMultipartResponse(item, strategy);
            } else {
                throw new ValidationException("Not a multipart upload");
            }
        } catch (FileUploadException ex) {
            throw new ApiErrorException("Error in multipart file upload", ex);
        }
    }

    private Response getMultipartResponse(final UploadItem uploadItem, final Function<UploadItem, UUID> strategy) {
        UUID item = strategy.apply(uploadItem);
        URI location = uriInfo.getBaseUriBuilder().
                path(ItemExposure.class).
                path(ItemExposure.class, "get").
                build(item.toString());
        return Response.created(location).build();
    }

    private static MultipartUploadItemRepresentation valid(final MultipartUploadItemRepresentation uploadItemRepresentation) {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<MultipartUploadItemRepresentation>> errors = validator.validate(uploadItemRepresentation, Default.class);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException("Error in multiparet upload for item with URI: "
                    + Objects.toString(uploadItemRepresentation.getUri()), errors);
        }
        return uploadItemRepresentation;
    }

    private static InputStream getInputStream(final FileItem m) {
        try {
            return m.getInputStream();
        } catch (IOException ex) {
            throw new ApiErrorException("Error processing multipart data - unable to get inputstream for reference " + m.getName(), ex);
        }
    }

    private String uriToString(final MultipartUploadItemRepresentation uploadItemRepresentation) {
        return (uploadItemRepresentation == null) ? "null" : Objects.toString(uploadItemRepresentation.getUri());
    }

    private Optional<MultipartUploadItemRepresentation> retrieveMultipartUploadItemRepresentation(final List<FileItem> multipartItems) {
        return multipartItems.stream()
                .filter(m -> UPLOAD_ITEM_METADATA_FORM_FIELD.equalsIgnoreCase(m.getFieldName()))
                .findAny()
                .map(f -> getUploadItemRepresentationFromFormMetadata(f.getString()))
                .map(ItemExposure::valid);
    }

    private Optional<MimeType> retrieveMimeType(final List<FileItem> multipartItems, final String ref) {
        return multipartItems.stream()
                .filter(i -> i.getFieldName().equals(ref))
                .findAny()
                .map(f -> f.getName())
                .flatMap(n -> multipartItems.stream()
                .filter(f -> f.getFieldName().equals(n))
                .findAny()
                .map(i -> MimeType.parse(i.getContentType()))
                );
    }

    private Optional<InputStream> retrieveInputStream(final List<FileItem> multipartItems, final String ref) {
        return multipartItems.stream()
                .filter(m -> m.getFieldName().equals(ref))
                .findAny()
                .map(ItemExposure::getInputStream);
    }

    private UploadItem processMultipartItems(final List<FileItem> multipartItems) {
        MultipartUploadItemRepresentation uploadItemRepresentation = retrieveMultipartUploadItemRepresentation(multipartItems)
                .orElseThrow(() -> new ValidationException("Error processing multipart data. Form field '" + UPLOAD_ITEM_METADATA_FORM_FIELD + "' not found"));

        InputStream is = retrieveInputStream(multipartItems, uploadItemRepresentation.getContentRef())
                .orElseThrow(() -> new ValidationException("Error processing multipart data. Content reference '"
                + uploadItemRepresentation.getContentRef() + "' not found as from field for item with URI: "
                + uriToString(uploadItemRepresentation)));

        MimeType m = retrieveMimeType(multipartItems, uploadItemRepresentation.getContentRef())
                .orElseThrow(() -> new ValidationException("Error processing multipart data. Mimetype not specified on attachement for item with URI: "
                + uriToString(uploadItemRepresentation)));

        return new MultipartUploadItemMapper().apply(
                new MultipartUploadItemMapper.MultipartUploadItem(uploadItemRepresentation, m, is));
    }

    private MultipartUploadItemRepresentation getUploadItemRepresentationFromFormMetadata(final String metadata) {
        ObjectMapper mapper = ObjectMapperFactory.create();
        try {
            return mapper.readValue(metadata, MultipartUploadItemRepresentation.class);
        } catch (IOException ex) {
            throw new ApiErrorException("Unable to parse metadata in multipart message", ex);
        }
    }

    private static boolean isMultipartContent(final HttpServletRequest request) {
        return FileUploadBase.isMultipartContent(new ServletRequestContext(request));
    }

}
