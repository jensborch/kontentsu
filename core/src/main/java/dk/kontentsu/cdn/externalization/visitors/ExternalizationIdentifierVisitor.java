package dk.kontentsu.cdn.externalization.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hashids.Hashids;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTree;

/**
 * Visitor for generating a unique id for a give combination of versions that makes up a temporal reference tree.
 *
 * The generated id is derived form the database id of the underlying versions and are thus not guaranteed globally unique.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalizationIdentifierVisitor implements ExternalizationVisitor {

    private static final String SALT = "Externalization salt";

    private final ExternalizationVisitor nested;

    private final List<Integer> identifiers;

    public ExternalizationIdentifierVisitor(final ExternalizationVisitor nested) {
        this.nested = nested;
        this.identifiers = new ArrayList<>();
    }

    @Override
    public TemporalReferenceTree.Visitor copy() {
        return new ExternalizationIdentifierVisitor((ExternalizationVisitor) nested.copy());
    }

    @Override
    public Content getContent() {
        return nested.getContent();
    }

    public Optional<String> getContentId() {
        return Optional.of(new Hashids(SALT).encode(identifiers.stream().sorted().mapToLong(i -> i).toArray()));
    }

    @Override
    public void visit(final TemporalReferenceTree.Node node) {
        identifiers.add(node.getVersion().getId());
        nested.visit(node);
    }

}
