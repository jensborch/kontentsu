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
package dk.kontentsu.cdn.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.internal.Metadata;

/**
 *
 * @author Jens Borch Christiansen
 */
public class ContentParser {

    ContentParser() {
    }

    public static ContentParser create(final Content content) {
        if (content.getMimeType().isHal()) {
            return new HalJsonParser(content);
        } else {
            return new ContentParser();
        }
    }

    public Results parse() {
        return new Results(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Result from parsing the content.
     */
    public static class Results {

        private final List<Link> composition;
        private final List<Metadata> metadata;

        Results(final List<Link> composition, final List<Metadata> metadata) {
            this.composition = composition;
            this.metadata = metadata;
        }

        public List<Link> getComposition() {
            return Collections.unmodifiableList(composition);
        }

        public List<Metadata> getMetadata() {
            return Collections.unmodifiableList(metadata);
        }
    }
}
