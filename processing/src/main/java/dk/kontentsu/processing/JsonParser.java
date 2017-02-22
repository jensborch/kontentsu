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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.jackson.ObjectMapperFactory;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.internal.Metadata;
import dk.kontentsu.model.internal.MetadataType;
import dk.kontentsu.parsers.ContentParser;
import dk.kontentsu.parsers.ContentParserException;
import dk.kontentsu.parsers.Link;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
            JsonNode jsonContent = objectMapper.readTree(content.getData());
            return new Results(parse(jsonContent), parseMetadata(jsonContent));
        } catch (IOException ex) {
            throw new ContentParserException("Unable to parse content for contetn with UUID: " + content.getUuid(), ex);
        }
    }

    private Map<Metadata.Key, Metadata> parseMetadata(final JsonNode jsonContent) {
        Map<Metadata.Key, Metadata> result = new HashMap<>();

        Optional.ofNullable(jsonContent.get(JSON_METADATA)).ifPresent(n -> n.fields().forEachRemaining(m -> {
            if (m.getValue().isArray()) {
                try {
                    List<String> values = objectMapper.readValue(m.getValue().traverse(), new TypeReference<List<String>>() {
                    });
                    String key = m.getKey();
                    String value = values.stream().collect(Collectors.joining(","));
                    result.put(new Metadata.Key(MetadataType.PAGE, key), new Metadata(value));
                } catch (IOException ex) {
                    LOGGER.debug("Error reading array", ex);
                }

            } else if (m.getValue().isValueNode()) {
                String value = m.getValue().asText();
                String key = m.getKey();
                LOGGER.debug("Adding metadata - key:{}, type:{}, value:{}", key, MetadataType.PAGE, value);
                result.put(new Metadata.Key(MetadataType.PAGE, key), new Metadata(value));
            } else {
                LOGGER.warn("Invalid metadata for key {}", m.getKey());
            }
        }));
        return result;
    }

    private List<Link> parse(final JsonNode jsonContent) {
        List<Link> result = new ArrayList<>();
        //TODO: parse single links
        List<JsonNode> linkNodes = jsonContent.findValues(JSON_LINK);
        List<JsonNode> linksNodes = jsonContent.findValues(JSON_LINKS);

        linksNodes.stream().forEach(n -> {
            if (n.findParent(JSON_COMPOSITION) != null) {
                //TODO: loop over composition links
                //LOGGER.debug("Adding composition {}", n.asText());
                //result.add(new Link(SemanticUri.parse(n.asText()), ReferenceType.COMPOSITION));
            } else {
                //TODO: loop over links
                //LOGGER.debug("Adding link {}", n.asText());
                //result.add(new Link(SemanticUri.parse(n.asText()), ReferenceType.LINK));
            }
        });
        return result;
    }
}
