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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dk.kontentsu.model.Metadata;

/**
 * Content parser interface. Class implementing this interface are responsible for parsing content and thus finding links, compositions and metadata in content.
 *
 * When uploading content <code>ContentParser</code> class implementing this will be instantiated and used.
 *
 * @author Jens Borch Christiansen
 */
public interface ContentParser {

    Results parse();

    /**
     * Result from parsing the content.
     */
    class Results {

        private final List<Link> links;
        private final Map<Metadata.Key, Metadata> metadata;

        public Results(final List<Link> links, final Map<Metadata.Key, Metadata> metadata) {
            this.links = links;
            this.metadata = metadata;
        }

        public List<Link> getLinks() {
            return Collections.unmodifiableList(links);
        }

        public Map<Metadata.Key, Metadata> getMetadata() {
            return Collections.unmodifiableMap(metadata);
        }
    }
}
