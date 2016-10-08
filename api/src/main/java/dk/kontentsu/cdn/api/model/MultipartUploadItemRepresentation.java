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
package dk.kontentsu.cdn.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import io.swagger.annotations.ApiModelProperty;

/**
 * Class representing the JSON data for uploading an item to the CDN using multipart.
 *
 * Mimetype and charset encoding must be specified at part of attachment content type - e.g. <code>application/hal+json;charset=utf-8</code>
 *
 * @author Jens Borch Christiansen
 */
@SuppressWarnings("PMD.ImmutableField")
public class MultipartUploadItemRepresentation {

    @NotNull
    @ApiModelProperty(value = "The (semantic) URI of the uploaded item on the CDN", dataType = "string", example = "images/book/book-xl.png", required = true)
    private SemanticUri uri;

    @ApiModelProperty(value = "Set to true if item should be a draft and thus not uploadet to the CDN", required = false)
    private boolean draft;

    @NotNull
    @Valid
    @JsonUnwrapped
    @ApiModelProperty(value = "Inteval item should be avilabe on the CDN", dataType = "java.time.ZonedDateTime", example = "2016-04-09:20:00:00Z", name = "from", required = true)
    private Interval interval;

    @NotNull
    @ApiModelProperty(value = "Reference to multipart attachments", required = true)
    private String contentRef;

    @ApiModelProperty(value = "Destination host for CDN item", example = "Website", required = false, notes = "If not specified item will be uploade to all registred hosts")
    private List<String> hosts = new ArrayList<>();

    public SemanticUri getUri() {
        return uri;
    }

    public boolean isDraft() {
        return draft;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getContentRef() {
        return contentRef;
    }

    public List<String> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

}
