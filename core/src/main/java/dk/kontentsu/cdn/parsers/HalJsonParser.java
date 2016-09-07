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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.kontentsu.cdn.jackson.ObjectMapperFactory;
import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.internal.Metadata;
import dk.kontentsu.cdn.model.internal.ReferenceType;

/**
 * Parser for HAL+JSON CDN content. The parser will find metadata and compositions in the data.
 *
 * @author Jens Borch Christiansen
 */
public class HalJsonParser extends ContentParser {

    public static final String JSON_LINKS = "_links";
    public static final String JSON_HREF = "href";
    public static final String JSON_SELF_LINK = "self";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_COMPOSITION_TYPE = "composition-type";
    public static final String JSON_COMPOSITION = "composition";
    public static final String JSON_REF = "ref";
    public static final String[] JSON_METADATA = {"seo"};

    private static final Logger LOGGER = LoggerFactory.getLogger(HalJsonParser.class);
    private final ObjectMapper objectMapper = ObjectMapperFactory.create();
    private final Content content;

    public HalJsonParser(final Content content) {
        this.content = content;
    }

    @Override
    public Results parse() {
        return new Results(parseComposition(), parseMetadata());
    }

    private List<Metadata> parseMetadata() {
        //TODO: Implement
        try {
            JsonNode jsonContent = objectMapper.readTree(content.getData());

        } catch (IOException ex) {

        }
        return new ArrayList<>();
    }

    private List<Link> parseComposition() {
        //TODO: Implement link parsing
        List<Link> result = new ArrayList<>();
        try {
            JsonNode jsonContent = objectMapper.readTree(content.getData());
            JsonNode compositionNode = jsonContent.findPath(JSON_LINKS).findPath(JSON_COMPOSITION);
            Iterator<Map.Entry<String, JsonNode>> it = compositionNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> parent = it.next();
                List<JsonNode> hrefs = parent.getValue().findValues(JSON_HREF);
                Optional<JsonNode> found = hrefs.stream().findFirst();
                if (found.isPresent()) {
                    result.add(new Link(SemanticUri.parse(found.get().asText()), ReferenceType.COMPOSITION));
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Error parsing content for coposition", ex);
        }
        return result;
    }

}
