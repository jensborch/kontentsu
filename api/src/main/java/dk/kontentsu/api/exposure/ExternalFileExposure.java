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

import java.time.ZonedDateTime;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.kontentsu.api.exposure.model.ErrorRepresentation;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.util.DateTimeFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Exposure for getting files the CDN.
 *
 * @author Jens Borch Christiansen
 */
@Path("/files")
@Stateless
@PermitAll
@Api(tags = {"files"})
public class ExternalFileExposure {

    @Inject
    private ExternalFileRepository repo;

    @GET
    @Path("/{uri:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get content as on the CDN",
            notes = "The accept header must match the mime type of the content at the given URI")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The content uploaded to the CDN"),
        @ApiResponse(code = 404, message = "No content found at URI", response = ErrorRepresentation.class),
        @ApiResponse(code = 406, message = "Accept header does not match resource mime type", response = ErrorRepresentation.class)})
    public Response get(
            @ApiParam(value = "URI to content on the CDN", required = true)
            @PathParam("uri") @NotNull @Size(min = 3)
            final String uri,
            @ApiParam(value = "Accept header defining the content type to retrieve", required = false)
            @HeaderParam(HttpHeaders.ACCEPT)
            final String acceptHeader,
            @ApiParam(value = "Timestamp in UTC format defining at what point in time to get content from", required = false)
            @QueryParam("at")
            @DateTimeFormat(DateTimeFormat.Format.UTC)
            final String at) {
        ZonedDateTime time = (at == null) ? null : ZonedDateTime.parse(at);
        ExternalFile result = repo.getByUri(SemanticUri.parse(uri), time);
        return getResponse(result, acceptHeader);
    }

    private Response getResponse(final ExternalFile file, final String acceptHeader) {
        if (file.getContent().getMimeType().matchesHeader(acceptHeader)) {
            Response.ResponseBuilder builder = Response
                    .status(Response.Status.OK)
                    .entity(file.getContent().getDataAsBinaryStream())
                    .type(file.getContent().getMimeType().toMediaType());
            file.getContent().getEncoding().ifPresent(e -> builder.encoding(e.toString()));
            return builder.build();
        } else {
            throw new MimeTypeMismatchException("Accept header " + acceptHeader + " does not match resource mime type " + file.getContent().getMimeType());
        }
    }

}
