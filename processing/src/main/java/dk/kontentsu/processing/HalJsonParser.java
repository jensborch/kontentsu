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

import static dk.kontentsu.processing.HalJsonContent.JSON_COMPOSITION;
import static dk.kontentsu.processing.HalJsonContent.JSON_HREF;
import static dk.kontentsu.processing.HalJsonContent.JSON_LINKS;
import static dk.kontentsu.processing.HalJsonContent.JSON_METADATA;
import static dk.kontentsu.processing.HalJsonContent.JSON_SELF_LINK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.jackson.ObjectMapperFactory;
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
 * Parser for HAL+JSON CDN content. The parser will find metadata and compositions in the data.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"application/hal+json"})
public class HalJsonParser implements ContentParser {

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    Content content;

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    @Override
    public Results parse() {
        try {
            JsonNode jsonContent = objectMapper.readTree(content.getData());
            return new Results(parse(jsonContent), parseMetadata(jsonContent));
        } catch (IOException ex) {
            throw new ContentParserException("Unable to parse content for content with UUID: " + content.getUuid(), ex);
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
                    result.put(new Metadata.Key(map(metadataType), metadata.getKey()), new Metadata(metadata.getValue().asText()));
                }
            }
        }
        return result;
    }

    private MetadataType map(final String type) {
        if ("seo".equalsIgnoreCase(type)) {
            return MetadataType.PAGE;
        } else {
            return MetadataType.OTHER;
        }
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
                    String l = found.get().asText();
                    LOGGER.debug("Adding link {}", l);
                    try {
                        result.add(new Link(new Item.URI(l), ReferenceType.LINK));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Unable to parse link {}", l, e);
                    }
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
            hrefs.forEach(found -> {
                if (found.isTextual()) {
                    LOGGER.debug("Adding composition {}", found.asText());
                    result.add(new Link(new Item.URI(found.asText()), ReferenceType.COMPOSITION));
                }
            });
        }
        return result;
    }
}
