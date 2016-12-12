package dk.kontentsu.cdn.externalization.visitors;

import java.util.Optional;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTreeVisitor;

/**
 *
 * @author Jens Borch Christiansen
 */
public abstract class ExternalizationVisitor implements TemporalReferenceTreeVisitor<ExternalizationVisitor.Results> {

    /**
     * Default result implementation used for externalized content.
     */
    public static class Results implements TemporalReferenceTreeVisitor.Results {

        final Optional<String> id;
        final Content content;

        public Results(final String id, final Content content) {
            this.id = Optional.of(id);
            this.content = content;
        }

        public Results(final Content content) {
            this.id = Optional.empty();
            this.content = content;
        }

        public Optional<String> getId() {
            return id;
        }

        public Content getContent() {
            return content;
        }
    }

}
