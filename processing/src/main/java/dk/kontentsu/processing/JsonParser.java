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
package dk.kontentsu.processing;

import static dk.kontentsu.processing.JsonContent.JSON_COMPOSITION;
import static dk.kontentsu.processing.JsonContent.JSON_HREF;
import static dk.kontentsu.processing.JsonContent.JSON_METADATA;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Metadata;
import dk.kontentsu.model.MetadataType;
import dk.kontentsu.model.ReferenceType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.parsers.ContentParserException;
import dk.kontentsu.parsers.Link;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parser for JSON content. The parser will find metadata and compositions in
 * the data.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"application/json"})
public class JsonParser implements ContentParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private com.fasterxml.jackson.core.JsonParser jsonParser;

    @Inject
    Content content;

    @PostConstruct
    public void init() {
        JsonFactory jf = new JsonFactory();
        try {
            jsonParser = jf.createParser(content.getData());
        } catch (IOException ex) {
            LOGGER.error("Error creating JSON parser", ex);
        }
    }

    @Override
    public Results parse() {
        try {
            Map<Metadata.Key, Metadata> metadata = new HashMap<>();
            List<Link> links = new ArrayList<>();
            Processor[] fieldProcessors = new Processor[3];
            fieldProcessors[0] = new Processor((p, f) -> {
                return JSON_HREF.equals(f) && p.contains(JSON_COMPOSITION);
            }, (k, v) -> {
                links.add(new Link(new Item.URI(v), ReferenceType.COMPOSITION));
            });
            fieldProcessors[1] = new Processor((p, f) -> {
                return JSON_HREF.equals(f)
                        && !p.contains(JSON_COMPOSITION)
                        && !"template".equals(p.peekLast())
                        && !"composition-type".equals(p.peekLast());
            }, (k, v) -> {
                links.add(new Link(new Item.URI(v), ReferenceType.LINK));
            });
            fieldProcessors[2] = new Processor((p, f) -> {
                return JSON_METADATA.equals(p.peekLast());
            }, (k, v) -> {
                LOGGER.debug("Adding metadata - key:{}, type:{}, value:{}", k, MetadataType.PAGE, v);
                metadata.put(new Metadata.Key(MetadataType.PAGE, k), new Metadata(v));
            });
            process(fieldProcessors);
            return new Results(links, metadata);
        } catch (IOException ex) {
            throw new ContentParserException("Unable to parse content for content with UUID: " + content.getUuid(), ex);
        }
    }

    private void process(final Processor... processors) throws IOException {
        String fieldName = null;
        Deque<String> path = new ArrayDeque<>();
        boolean array = false;
        while (!jsonParser.isClosed()) {
            JsonToken token = jsonParser.nextToken();
            if (token == JsonToken.FIELD_NAME) {
                fieldName = jsonParser.getCurrentName();
                for (Processor p : processors) {
                    if (p.fieldMatcher.apply(path, fieldName)) {
                        jsonParser.nextToken();
                        p.fieldConsumer.accept(fieldName, jsonParser.getValueAsString());
                    }
                }
            }
            if (token == JsonToken.START_ARRAY) {
                array = true;
            }
            if (token == JsonToken.END_ARRAY) {
                array = false;
            }
            if (token == JsonToken.START_OBJECT && fieldName != null && !array) {
                path.push(fieldName);
            }
            if (token == JsonToken.END_OBJECT && !path.isEmpty() && !array) {
                path.pop();
            }
            if (token == JsonToken.END_OBJECT) {
                fieldName = null;
            }
        }
    }

    /**
     * Processor for a JSON field.
     */
    private static class Processor {

        final BiConsumer<String, String> fieldConsumer;

        final BiFunction<Deque<String>, String, Boolean> fieldMatcher;

        Processor(
                final BiFunction<Deque<String>, String, Boolean> fieldMatcher,
                final BiConsumer<String, String> fieldConsumer) {
            this.fieldConsumer = fieldConsumer;
            this.fieldMatcher = fieldMatcher;
        }
    }
}
