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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import dk.kontentsu.model.Approver;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.State;
import dk.kontentsu.model.Version;
import io.swagger.annotations.ApiModelProperty;

/**
 * Representation of a version of an item in the CDN.
 *
 * @author Jens Borch Christiansen
 */
public class VersionRepresentation {

    private final Map<Metadata.Key, Metadata> metadata = new HashMap<>();

    @ApiModelProperty(value = "Mime type of the version", required = true)
    private final String mimeType;

    @JsonUnwrapped
    private final Interval interval;

    @ApiModelProperty(value = "The state of this version - e.g. draft", required = true)
    private final State state;

    @ApiModelProperty(value = "The person who approved this version", required = false)
    private final Approver approver;

    @ApiModelProperty(value = "List of references", required = false)
    private final List<ReferenceRepresentation> references;

    public VersionRepresentation(final Version v, final UriInfo uriInfo) {
        this.mimeType = v.getContent().getMimeType().toString();
        this.interval = v.getInterval();
        this.state = v.getState();
        this.approver = v.getApprover();
        this.references = v.getReferences().stream().map(r -> new ReferenceRepresentation(r, uriInfo)).collect(Collectors.toList());
        metadata.putAll(v.getMetadata());
    }

    public State getState() {
        return state;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Approver getApprover() {
        return approver;
    }

    public List<ReferenceRepresentation> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public Map<Metadata.Key, Metadata> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

}
