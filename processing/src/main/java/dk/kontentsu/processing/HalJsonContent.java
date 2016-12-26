package dk.kontentsu.processing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for HAL+JSON content.
 *
 * @author Jens Borch Christiansen
 */
public final class HalJsonContent {

    public static final String JSON_COMPOSITION = "composition";
    public static final String JSON_COMPOSITION_TYPE = "composition-type";
    public static final String JSON_CONTENT = "content";
    public static final String JSON_HREF = "href";
    public static final String JSON_LINKS = "_links";
    public static final List<String> JSON_METADATA = Collections.unmodifiableList(Arrays.asList(new String[]{"seo"}));
    public static final String JSON_REF = "ref";
    public static final String JSON_SELF_LINK = "self";

    private HalJsonContent() {
    }

}
