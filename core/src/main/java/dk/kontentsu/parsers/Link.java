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
package dk.kontentsu.parsers;

import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.SemanticUriPath;
import dk.kontentsu.model.internal.ReferenceType;

/**
 * Link in content
 */
public class Link {

    private final ReferenceType type;
    private final SemanticUri uri;

    public Link(final SemanticUri uri, final ReferenceType type) {
        this.uri = uri;
        this.type = type;
    }

    public ReferenceType getType() {
        return type;
    }

    public SemanticUri getUri() {
        return uri;
    }

    public SemanticUriPath getPath() {
        return uri.getPath();
    }

    public String getName() {
        return uri.getName();
    }
}
