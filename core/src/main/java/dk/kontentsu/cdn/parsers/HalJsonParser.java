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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.cdn.jackson.ObjectMapperFactory;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.internal.Metadata;
import dk.kontentsu.cdn.model.internal.ReferenceType;
import dk.kontentsu.cdn.spi.ContentScoped;
import dk.kontentsu.cdn.spi.Parsable;

/**
 * Parser for HAL+JSON CDN content. The parser will find metadata and compositions in the data.
 *
 * @author Jens Borch Christiansen
 */
@ContentScoped
public class HalJsonParser implements ContentParser, HalJsonContent {


    private static final Logger LOGGER = LoggerFactory.getLogger(HalJsonParser.class);
    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    @Inject
    @ContentScoped
    private Parsable content;

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
        for (String metadataType : JSON_METADATA) {
            JsonNode linksNode = jsonContent.findPath(metadataType);
            Iterator<Map.Entry<String, JsonNode>> it = linksNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> metadata = it.next();
                if (metadata.getValue().isValueNode()) {
                    LOGGER.debug("Adding metadata - key:{}, type:{}, value:{}", metadata.getKey(), metadataType, metadata.getValue().asText());
                    result.put(new Metadata.Key(metadataType, metadata.getKey()), new Metadata(metadata.getValue().asText()));
                }
            }
        }
        return result;
    }

    private List<Link> parse(final JsonNode jsonContent) {
        List<Link> result = new ArrayList<>(parseComposition(jsonContent));
        result.addAll(parseLinks(jsonContent));
        return result;
    }

    private List<Link> parseLinks(final JsonNode jsonContent) {
        List<Link> result = new ArrayList<>();
        JsonNode linksNode = jsonContent.findPath(JSON_LINKS);
        Iterator<Map.Entry<String, JsonNode>> it = linksNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> link = it.next();
            if (!JSON_SELF_LINK.equals(link.getKey()) && !JSON_COMPOSITION.equals(link.getKey()) && !link.getKey().contains("template")) {
                List<JsonNode> hrefs = link.getValue().findValues(JSON_HREF);
                Optional<JsonNode> found = hrefs.stream().findFirst();
                if (found.isPresent() && found.get().isTextual()) {
                    LOGGER.debug("Adding link {}", found.get().asText());
                    result.add(new Link(SemanticUri.parse(found.get().asText()), ReferenceType.LINK));
                }
            }
        }
        return result;
    }

    private List<Link> parseComposition(final JsonNode jsonContent) {
        List<Link> result = new ArrayList<>();
        JsonNode compositionNode = jsonContent.findPath(JSON_LINKS).findPath(JSON_COMPOSITION);
        Iterator<Map.Entry<String, JsonNode>> it = compositionNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> parent = it.next();
            List<JsonNode> hrefs = parent.getValue().findValues(JSON_HREF);
            hrefs.stream().forEach(found -> {
                if (found.isTextual()) {
                    LOGGER.debug("Adding composition {}", found.asText());
                    result.add(new Link(SemanticUri.parse(found.asText()), ReferenceType.COMPOSITION));
                }
            });
        }
        return result;
    }

    /**
     * Factory for creating JSON content parsers.
     */
    /*@Dependent
    public static class Factory implements ContentParser.Factory {

        @Override
        public Optional<ContentParser> create(final Content content) {
            if (content.getMimeType().isHal()) {
                return Optional.of(new HalJsonParser(content));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName();
        }

    }*/

}
