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
package dk.kontentsu.externalization.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.kontentsu.externalization.ExternalizationException;
import dk.kontentsu.jackson.ObjectMapperFactory;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.internal.TemporalReferenceTree;
import dk.kontentsu.model.internal.Version;
import static dk.kontentsu.parsers.HalJsonContent.*;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Tree visitor for creating external HAL+JSON page content.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"application/hal+json"})
public class HalJsonExternalizationVisitor extends ExternalizationVisitor {

    private JsonNode pageNode;
    private JsonNode contentNode;
    private final Counter counter;

    private final ObjectMapper mapper;

    @Inject
    private Content content;

    public HalJsonExternalizationVisitor() {
        this.counter = new Counter();
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
        removeComposition(pageNode);
        Content result = new Content(pageNode.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, content.getMimeType());
        return new ExternalizationVisitor.Results(result);
    }

    private String getErrorMsg() {
        return "Error externalizing content for with id: " + content.getUuid();
    }

    @Override
    public void visit(final TemporalReferenceTree.Node node) {
        try {
            if (!node.isRoot()) {
                JsonNode newContentNode = mapper.readTree(node.getVersion().getContent().getDataAsBinaryStream());
                Map.Entry<String, JsonNode> parentNode = getContentNodeTypes(pageNode, node.getVersion());
                updateLinksInExternalContent(pageNode, newContentNode, parentNode.getKey(), counter);
                addContent2ExternalContent(contentNode, newContentNode, parentNode);
            }
        } catch (IOException ex) {
            throw new ExternalizationException(getErrorMsg(), ex);
        }
    }

    private void removeComposition(final JsonNode pageNode) {
        JsonNode tmp = pageNode.findPath(JSON_LINKS);
        if (tmp instanceof ObjectNode) {
            ObjectNode links = (ObjectNode) tmp;
            links.remove(JSON_COMPOSITION);
        }
    }

    private void updateLinksInExternalContent(final JsonNode external,
            final JsonNode newContentNode,
            final String type,
            final Counter counter) {
        ObjectNode linksNodeToUpdate = (ObjectNode) external.findPath(JSON_LINKS);
        JsonNode linksNode = newContentNode.findPath(JSON_LINKS);

        Consumer<Map.Entry<String, JsonNode>> linksUpdater = (Map.Entry<String, JsonNode> node) -> {
            if (node.getValue().get(JSON_HREF) != null && !node.getKey().equals(JSON_SELF_LINK)) {
                String newRef = type + counter.get(type) + "-" + node.getKey();
                ObjectNode href = linksNodeToUpdate.with(newRef);
                href.set(JSON_HREF, node.getValue().get(JSON_HREF));

                updateContentNodeRefs(newContentNode, node.getKey(), newRef);
            }
        };

        if (linksNode.fields().hasNext()) {
            counter.getAndIncrement(type);
            linksNode.fields().forEachRemaining(linksUpdater);
        }

        ((ObjectNode) newContentNode).remove(JSON_LINKS);
    }

    private static void updateContentNodeRefs(final JsonNode newContentNode, final String oldName, final String newName) {
        newContentNode.findParents(JSON_REF).stream()
                .filter(n -> n.get(JSON_REF).asText().equals(oldName))
                .forEach(n -> ((ObjectNode) n).put(JSON_REF, newName));
    }

    private void addContent2ExternalContent(final JsonNode externalContentNode,
            final JsonNode newContentNode,
            final Map.Entry<String, JsonNode> parentNode) {
        String propertyName = parentNode.getKey();
        ObjectNode tmpContentNode = (ObjectNode) externalContentNode;
        if (parentNode.getValue().isArray()) {
            tmpContentNode.withArray(propertyName).add(newContentNode);
        } else {
            tmpContentNode.set(propertyName, newContentNode);
        }
    }

    private Map.Entry<String, JsonNode> getContentNodeTypes(final JsonNode content, final Version version) {
        JsonNode compositionNode = content.findPath(JSON_LINKS).findPath(JSON_COMPOSITION);
        Iterator<Map.Entry<String, JsonNode>> it = compositionNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> parent = it.next();
            List<JsonNode> hrefs = parent.getValue().findValues(JSON_HREF);
            Optional<JsonNode> found = hrefs.stream()
                    .filter(href -> version.getItem().getUri().matches(href.asText()))
                    .findFirst();
            if (found.isPresent()) {
                return parent;
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

    /**
     * Counter for creating unique link names in finale "page"
     */
    private static class Counter {

        @SuppressWarnings("PMD.UseConcurrentHashMap")
        final Map<String, AtomicInteger> counter = new HashMap<>();

        AtomicInteger getAtomic(final String key) {
            this.counter.computeIfAbsent(key, k -> new AtomicInteger(0));
            return this.counter.get(key);
        }

        int get(final String key) {
            return getAtomic(key).intValue();
        }

        int getAndIncrement(final String key) {
            return getAtomic(key).getAndIncrement();
        }
    }
}
