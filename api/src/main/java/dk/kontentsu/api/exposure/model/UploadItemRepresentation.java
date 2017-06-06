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
package dk.kontentsu.api.exposure.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.SemanticUri;
import io.swagger.annotations.ApiModelProperty;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Class representing the JSON data for uploading an item to the CDN.
 *
 * @author Jens Borch Christiansen
 */
public class UploadItemRepresentation {

    @NotNull
    @ApiModelProperty(value = "The (semantic) URI of the uploaded item on the CDN",
            dataType = "string",
            example = "images/book/book-xl.png",
            required = true)
    private SemanticUri uri;

    @ApiModelProperty(value = "Set to true if the item should be a draft and thus not uploadet to the CDN host", required = false)
    private boolean draft;

    @NotNull
    @Valid
    @JsonUnwrapped
    @ApiModelProperty(value = "Timestamp from when an item should be avilabe on the CDN",
            notes = "To specify a end date for the item use 'to'",
            dataType = "java.time.ZonedDateTime",
            example = "2016-04-09T20:00:00Z",
            name = "from",
            required = true)
    private Interval interval;

    @NotNull
    @ApiModelProperty(value = "Mimetype of the content uploaded", dataType = "string", example = "image/png", required = true)
    private MimeType mimeType;

    @ApiModelProperty(value = "Encoding of the content uploaded",
            notes = "Not required for binary content",
            example = "utf-8",
            required = false)
    private String encoding;

    @NotNull
    @ApiModelProperty(value = "URL to content to upload", example = "http://server/images/image.png", required = true)
    private URL contentUrl;

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

    public MimeType getMimeType() {
        return mimeType;
    }

    public URL getContentUrl() {
        return contentUrl;
    }

    public String getEncoding() {
        return encoding;
    }

    public List<String> getHost() {
        return Collections.unmodifiableList(hosts);
    }

}
