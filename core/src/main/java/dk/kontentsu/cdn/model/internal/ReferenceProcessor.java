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

import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTree.Node;
import dk.kontentsu.cdn.spi.ContentContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Breadth-first tree processor for finding all temporal versions of items that
 * can become an external pages.
 *
 * @author Jens Borch Christiansen
 * @param <V> the type of the visitor used by the processor
 */
public class ReferenceProcessor<R extends TemporalReferenceTree.Results, V extends TemporalReferenceTree.Visitor<R>> {

    private final Deque<Node> nodes = new ArrayDeque<>();
    private final Deque<TemporalReferenceTree<R, V>> processing = new ArrayDeque<>();
    private final List<TemporalReferenceTree<R, V>> processed = new ArrayList<>();

    public ReferenceProcessor(final Version root, final V visitor) {
        TemporalReferenceTree<R, V> tree = new TemporalReferenceTree<>(root, visitor);
        processing.push(tree);
    }

    public List<TemporalReferenceTree<R, V>> process() {
        while (!processing.isEmpty()) {
            ContentContext.execute(() -> {
                processInScope();
            });
        };
        return Collections.unmodifiableList(processed);
    }

    public void processInScope() {
        TemporalReferenceTree<R, V> tree = processing.pop();
        nodes.push(tree.getRoot());
        while (!nodes.isEmpty()) {
            Node current = nodes.pop();
            Map<SemanticUri, List<Version>> comps = current.getVersion().getComposition();
            if (comps.isEmpty() && !processed.contains(tree)) {
                processed.add(tree);
            }
            for (Map.Entry<SemanticUri, List<Version>> versions : comps.entrySet()) {
                boolean first = true;
                Interval startInterval = tree.getInteval();
                for (Version child : versions.getValue()) {
                    Optional<Interval> i = child.getInterval().intersection(startInterval);
                    if (i.isPresent()) {
                        if (first) {
                            tree.setInteval(i.get());
                            Node childNode = current.addChild(child);
                            nodes.push(childNode);
                            first = false;
                        } else {
                            processing.push(new TemporalReferenceTree<>(tree, i.get()));
                        }
                    }
                }
            }
        }
        tree.finalizeResults();
    }
}
