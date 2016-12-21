package dk.kontentsu.externalization.visitors;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.internal.TemporalReferenceTreeVisitor;

/**
 *
 * @author Jens Borch Christiansen
 */
public abstract class ExternalizationVisitor implements TemporalReferenceTreeVisitor<ExternalizationVisitor.Results> {

    /**
     * Default result implementation used for externalized content.
     */
    public static class Results implements TemporalReferenceTreeVisitor.Results {

        final Content content;

        public Results(final Content content) {
            this.content = content;
        }

        public Content getContent() {
            return content;
        }
    }

}
