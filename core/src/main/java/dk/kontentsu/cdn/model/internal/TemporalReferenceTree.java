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
package dk.kontentsu.cdn.model.internal;

import dk.kontentsu.cdn.externalization.visitors.ExternalizationVisitor;
import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.Interval;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Representation of a item that can be externalized - i.e. an page on the CDN
 * valid in a certain interval.
 *
 * To create actual external content, implement a Visitor to build the content
 * when traversing the tree.
 *
 * @author Jens Borch Christiansen
 * @param <V> type of the visitor use when processing the tree
 */
public class TemporalReferenceTree<V extends TemporalReferenceTree.Visitor> {

    private Interval inteval;
    private final Node root;
    private V visitor;
    private Optional<String> id;
    private Content content;

    public TemporalReferenceTree(final Version version, final V visitor) {
        this.inteval = version.getInterval();
        this.visitor = visitor;
        this.root = new Node(version, this);
        visitor.visit(root);
        //this.id = ((ExternalizationVisitor) visitor).getContentId();
        //this.content = ((ExternalizationVisitor) visitor).getContent();
    }

    /**
     * Copy constructor, creating a shallow copy with new interval.
     *
     * @param tree the existing tree to create a copy from
     * @param interval new interval to use
     */
    @SuppressWarnings("unchecked")
    public TemporalReferenceTree(final TemporalReferenceTree tree, final Interval interval) {
        this.inteval = interval;
        //this.visitor = (V) tree.visitor.copy();
        this.visitor = (V) tree.visitor;
        this.root = new Node(tree.getRoot().getVersion(), this);
        //ContentContext.execute(() -> {
        visitor.visit(root);
        //this.id = ((ExternalizationVisitor) visitor).getContentId();
        //this.content = ((ExternalizationVisitor) visitor).getContent();
        //});
    }

    public void finalizeResults() {
        this.id = ((ExternalizationVisitor) visitor).getContentId();
        this.content = ((ExternalizationVisitor) visitor).getContent();
    }

    public Interval getInteval() {
        return inteval;
    }

    void setInteval(final Interval inteval) {
        this.inteval = inteval;
    }

    /*public V getVisitor() {
        return visitor;
    }*/
    public Content getContent() {
        return content;
    }

    public Optional<String> getContentId() {
        return id;
    }

    public Node getRoot() {
        return root;
    }

    /**
     * Node in temporal reference tree.
     */
    public static final class Node {

        private final TemporalReferenceTree tree;
        private final Version version;
        private final Node parent;
        private final List<Node> children;

        private Node(final Version version, final Node parent, final TemporalReferenceTree tree) {
            this.version = version;
            this.children = new LinkedList<>();
            this.tree = tree;
            this.parent = parent;
        }

        private Node(final Version version, final TemporalReferenceTree tree) {
            this(version, null, tree);
        }

        Node addChild(final Version child) {
            Node childNode = new Node(child, this, tree);
            children.add(childNode);
            tree.visitor.visit(childNode);
            return childNode;
        }

        public List<Node> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public Optional<Node> getParent() {
            return Optional.ofNullable(parent);
        }

        public boolean isRoot() {
            return parent == null;
        }

        public Version getVersion() {
            return version;
        }

    }

    /**
     * Node visitor for temporal reference tree.
     */
    public interface Visitor {

        /**
         * Called when a new node is reached.
         *
         * @param node the node visited
         */
        void visit(Node node);

    }

}
