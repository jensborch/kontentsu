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
package dk.kontentsu.api.model;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import dk.kontentsu.api.exposure.ItemExposure;
import dk.kontentsu.model.Interval;
import dk.kontentsu.model.State;
import dk.kontentsu.model.Version;

/**
 *
 * @author Jens Borch Christiansen
 */
public class VersionLinkRepresentation {

    private final Link link;

    @JsonUnwrapped
    private final Interval interval;

    private final State state;

    public VersionLinkRepresentation(final Version v, final UriInfo uriInfo) {
        this.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder()
                .path(ItemExposure.class)
                .path(ItemExposure.class, "getVersion"))
                .rel("version")
                .build(v.getItem().getUuid(), v.getUuid());
        this.interval = v.getInterval();
        this.state = v.getState();
    }

    public Interval getInterval() {
        return interval;
    }

    public Link getLink() {
        return link;
    }

    public State getState() {
        return state;
    }

}
