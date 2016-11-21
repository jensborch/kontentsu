package dk.kontentsu.cdn.parsers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for HAL+JSON content.
 *
 * @author Jens Borch Christiansen
 */
public final class HalJsonContent {

    public final static String JSON_COMPOSITION = "composition";
    public final static String JSON_COMPOSITION_TYPE = "composition-type";
    public final static String JSON_CONTENT = "content";
    public final static String JSON_HREF = "href";
    public final static String JSON_LINKS = "_links";
    public final static List<String> JSON_METADATA = Collections.unmodifiableList(Arrays.asList(new String[]{"seo"}));
    public final static String JSON_REF = "ref";
    public final static String JSON_SELF_LINK = "self";

    private HalJsonContent() {
    }

}
