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

import static dk.kontentsu.processing.JsonContent.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.internal.Metadata;
import dk.kontentsu.model.internal.MetadataType;
import dk.kontentsu.model.internal.ReferenceType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.parsers.ContentParserException;
import dk.kontentsu.parsers.Link;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for JSON content. The parser will find metadata and compositions in
 * the data.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"application/json"})
public class JsonParser implements ContentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);
    private com.fasterxml.jackson.core.JsonParser jp;

    @Inject
    private Content content;

    @PostConstruct
    public void init() {
        JsonFactory jf = new JsonFactory();
        try {
            jp = jf.createParser(content.getData());
        } catch (IOException ex) {
            LOGGER.error("Error creating JSON parser", ex);
        }
    }

    @Override
    public Results parse() {
        try {
            Map<Metadata.Key, Metadata> metadata = new HashMap<>();
            List<Link> links = new ArrayList<>();
            List<Processor> fieldProcessors = new ArrayList<>();
            fieldProcessors.add(new Processor(
                    (p, f) -> {
                        return JSON_HREF.equals(f) && p.contains(JSON_COMPOSITION);
                    },
                    (k, v) -> {
                        links.add(new Link(SemanticUri.parse(v), ReferenceType.LINK));
                    })
            );
            fieldProcessors.add(new Processor(
                    (p, f) -> {
                        return JSON_HREF.equals(f)
                        && !p.contains(JSON_COMPOSITION)
                        && !"template".equals(p.peekLast())
                        && !"composition-type".equals(p.peekLast());
                    },
                    (k, v) -> {
                        links.add(new Link(SemanticUri.parse(v), ReferenceType.COMPOSITION));
                    })
            );
            fieldProcessors.add(new Processor(
                    (p, f) -> {
                        return JSON_METADATA.equals(p.peekLast());
                    },
                    (k, v) -> {
                        LOGGER.debug("Adding metadata - key:{}, type:{}, value:{}", k, MetadataType.PAGE, v);
                        metadata.put(new Metadata.Key(MetadataType.PAGE, k), new Metadata(v));
                    })
            );
            process(fieldProcessors);
            return new Results(links, metadata);
        } catch (IOException ex) {
            throw new ContentParserException("Unable to parse content for contetn with UUID: " + content.getUuid(), ex);
        }
    }

    private void process(List<Processor> processors) throws IOException {
        String fieldName = null;
        Deque<String> path = new ArrayDeque<>();
        while (!jp.isClosed()) {
            JsonToken token = jp.nextToken();
            if (token == JsonToken.FIELD_NAME) {
                fieldName = jp.getCurrentName();
                for (Processor p : processors) {
                    if (p.fieldMatcher.apply(path, fieldName)) {
                        jp.nextToken();
                        p.fieldConsumer.accept(fieldName, jp.getValueAsString());
                    }
                }
            }
            if (token == JsonToken.START_OBJECT && fieldName != null) {
                path.push(fieldName);
            }
            if (token == JsonToken.END_OBJECT && !path.isEmpty()) {
                path.pop();
            }
            if (token == JsonToken.END_OBJECT) {
                fieldName = null;
            }
        }
    }

    private static class Processor {

        BiConsumer<String, String> fieldConsumer;

        BiFunction<Deque<String>, String, Boolean> fieldMatcher;

        Processor(BiFunction<Deque<String>, String, Boolean> fieldMatcher, BiConsumer<String, String> fieldConsumer) {
            this.fieldConsumer = fieldConsumer;
            this.fieldMatcher = fieldMatcher;
        }
    }
}