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

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.State;
import dk.kontentsu.cdn.model.internal.Version;

/**
 *
 * @author Jens Borch Christiansen
 */
public class VersionRepresentation {

    private final String mimeType;

    @JsonUnwrapped
    private final Interval interval;

    private final State state;

    public VersionRepresentation(final Version v) {
        this.mimeType = v.getContent().getMimeType().toString();
        this.interval = v.getInterval();
        this.state = v.getState();
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

}
