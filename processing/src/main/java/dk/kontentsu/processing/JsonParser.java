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
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.jackson.ObjectMapperFactory;
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
    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    @Inject
    private Content content;

    @Override
    public Results parse() {
        try {
            JsonFactory jf = new JsonFactory();
            com.fasterxml.jackson.core.JsonParser jp = jf.createParser(content.getData());

            Map<Metadata.Key, Metadata> metadata = new HashMap<>();
            List<Link> links = new ArrayList<>();
            String fieldName = null;
            Deque<String> path = new ArrayDeque<>();
            while (!jp.isClosed()) {
                JsonToken token = jp.nextToken();

                if (token == JsonToken.FIELD_NAME) {
                    fieldName = jp.getCurrentName();
                    if (JSON_HREF.equals(fieldName) && !"template".equals(path.peekLast())
                            && !"composition-type".equals(path.peekLast())) {
                        jp.nextToken();
                        String link = jp.getValueAsString();
                        if (path.contains(JSON_COMPOSITION)) {
                            LOGGER.debug("Adding composition: {}", link);
                            links.add(new Link(SemanticUri.parse(link), ReferenceType.COMPOSITION));
                        } else {
                            LOGGER.debug("Adding link: {}", link);
                            links.add(new Link(SemanticUri.parse(link), ReferenceType.LINK));
                        }
                    } else if (JSON_METADATA.equals(path.peekLast())) {
                        jp.nextToken();
                        LOGGER.debug("Adding metadata - key:{}, type:{}, value:{}", fieldName, MetadataType.PAGE, jp.getValueAsString());
                        metadata.put(new Metadata.Key(MetadataType.PAGE, fieldName), new Metadata(jp.getValueAsString()));
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
            return new Results(links, metadata);
        } catch (IOException ex) {
            throw new ContentParserException("Unable to parse content for contetn with UUID: " + content.getUuid(), ex);
        }
    }
}
