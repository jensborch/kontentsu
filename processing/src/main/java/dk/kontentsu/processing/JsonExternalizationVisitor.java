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
import static dk.kontentsu.processing.JsonContent.JSON_CONTENT;
import static dk.kontentsu.processing.JsonContent.JSON_HREF;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.kontentsu.externalization.ExternalizationException;
import dk.kontentsu.externalization.ExternalizationVisitor;
import dk.kontentsu.jackson.ObjectMapperFactory;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.Version;
import dk.kontentsu.model.processing.TemporalReferenceTree;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import dk.kontentsu.spi.ScopedContent;

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
    ScopedContent content;

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
        Content result = new Content(pageNode.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
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
        ObjectNode tmpContentNode = (ObjectNode) externalContentNode;
        if (parentNode.array) {
            tmpContentNode.withArray(parentNode.name).add(newContentNode);
        } else {
            tmpContentNode.set(parentNode.name, newContentNode);
        }
    }

    private CompositionNode getContentNodeTypes(final JsonNode content, final Version version) {
        Iterator<Map.Entry<String, JsonNode>> i = content.findPath(JSON_COMPOSITION).fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> node = i.next();
            List<JsonNode> found = node.getValue().findParents(JSON_HREF);
            for (JsonNode n : found) {
                if (version.getItem().getUri().equals(new Item.URI(n.get(JSON_HREF).asText()))) {
                    return new CompositionNode(node.getKey(), node.getValue().isArray());
                }
            }
        }
        throw new ExternalizationException("Did not find composition with URI " + version.getItem().getUri().toString() + " in content");
    }

    private JsonNode findOrCreateExternalContentNode(final JsonNode node) {
        JsonNode externalContentNode = node.findPath(JSON_CONTENT);
        if (externalContentNode.isMissingNode()) {
            externalContentNode = node.with(JSON_CONTENT);
        }
        return externalContentNode;
    }

    /**
     * Composition JSON node with a name.
     */
    private static class CompositionNode {

        final boolean array;
        final String name;

        CompositionNode(final String name, final boolean array) {
            this.name = name;
            this.array = array;
        }

    }

}
