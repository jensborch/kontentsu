package dk.kontentsu.cdn.cdi;

import dk.kontentsu.cdn.model.Content;
import javax.inject.Inject;

/**
 *
 * @author Jens Borch Christiansen
 */
@ContentScoped
public class TestContentParser {

    @Inject
    private Content content;

    public Content getContent() {
        return content;
    }

}
