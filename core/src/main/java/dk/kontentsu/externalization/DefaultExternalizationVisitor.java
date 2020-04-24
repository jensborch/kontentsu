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
package dk.kontentsu.externalization;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.processing.TemporalReferenceTree;
import dk.kontentsu.repository.ItemRepository;
import dk.kontentsu.spi.ContentProcessingMimeType;
import dk.kontentsu.spi.ContentProcessingScoped;
import dk.kontentsu.spi.ScopedContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default externalization visitor for content that should not be processed.
 * Will throw a {@link ExternalizationException} if the content have a
 * composition.
 *
 * @author Jens Borch Christiansen
 */
@ContentProcessingScoped
@ContentProcessingMimeType({"*/*"})
public class DefaultExternalizationVisitor extends ExternalizationVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExternalizationVisitor.class);

    @Inject
    private ScopedContent content;

    @Inject
    private ItemRepository repo;

    @PostConstruct
    public void init() {
        LOGGER.debug("Using default externalization visitor for content {}", content.getUuid());
    }

    @Override
    public void visit(final TemporalReferenceTree.Node node) {
        if (!node.isRoot()) {
            throw new ExternalizationException("Content with UUID " + content.getUuid() + " is not processed for externalization and should thus not have a composition");
        }
    }

    @Override
    public ExternalizationVisitor.Results getResults() {
        //Injected content will be out of content processing CDI scope... so we need to get in from the DB
        Content tmp = repo.getContent(content.getUuid());
        return new ExternalizationVisitor.Results(tmp);
    }

}
