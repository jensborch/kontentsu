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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.kontentsu.externalization.ExternalizationException;
import dk.kontentsu.externalization.visitors.ExternalizationVisitor;
import dk.kontentsu.jackson.ObjectMapperFactory;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.internal.TemporalReferenceTree;
import dk.kontentsu.model.internal.Version;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Tree visitor for creating external JSON page content.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"application/json"})
public class JsonExternalizationVisitor extends ExternalizationVisitor {

    private JsonNode pageNode;
    private JsonNode contentNode;

    private final ObjectMapper mapper;

    @Inject
    private Content content;

    public JsonExternalizationVisitor() {
        this.mapper = ObjectMapperFactory.create();
    }

    @PostConstruct
    public void init() {
        try {
            pageNode = mapper.readTree(content.getDataAsBinaryStream());
            contentNode = findOrCreateExternalContentNode(pageNode);
        } catch (IOException ex) {
            throw new ExternalizationException(getErrorMsg(), ex);
        }
    }

    @Override
    public ExternalizationVisitor.Results getResults() {
        Content result = new Content(pageNode.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, content.getMimeType());
        return new ExternalizationVisitor.Results(result);
    }

    private String getErrorMsg() {
        return "Error externalizing content with id: " + content.getUuid();
    }

    @Override
    public void visit(final TemporalReferenceTree.Node node) {
        try {
            if (!node.isRoot()) {
                JsonNode newContentNode = mapper.readTree(node.getVersion().getContent().getDataAsBinaryStream());
                CompositionNode parentNode = getContentNodeTypes(pageNode, node.getVersion());
                addContent2ExternalContent(contentNode, newContentNode, parentNode);
            }
        } catch (IOException ex) {
            throw new ExternalizationException(getErrorMsg(), ex);
        }
    }

    private void addContent2ExternalContent(final JsonNode externalContentNode,
            final JsonNode newContentNode,
            final CompositionNode parentNode) {
        String propertyName = parentNode.name;
        ObjectNode tmpContentNode = (ObjectNode) externalContentNode;
        if (parentNode.node.isArray()) {
            tmpContentNode.withArray(propertyName).add(newContentNode);
        } else {
            tmpContentNode.set(propertyName, newContentNode);
        }
    }

    private class CompositionNode {

        String name;
        JsonNode node;

        public CompositionNode(String name, JsonNode node) {
            this.name = name;
            this.node = node;
        }

    }

    private CompositionNode getContentNodeTypes(final JsonNode content, final Version version) {
        Iterator<Map.Entry<String, JsonNode>> i = content.get(JSON_COMPOSITION).fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> node = i.next();
            List<JsonNode> found = node.getValue().findParents(JSON_HREF);
            for (JsonNode n : found) {
                if (version.getItem().getUri().equals(n.get(JSON_HREF).asText())) {
                    return new CompositionNode(node.getKey(), n);
                }
            }
        }
        throw new ExternalizationException("Did not finde composition with URI " + version.getItem().getUri().toString() + " in content");
    }

    private JsonNode findOrCreateExternalContentNode(final JsonNode node) {
        JsonNode externalContentNode = node.findPath(JSON_CONTENT);
        if (externalContentNode.isMissingNode()) {
            externalContentNode = node.with(JSON_CONTENT);
        }
        return externalContentNode;
    }

}
