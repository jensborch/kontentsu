package dk.kontentsu.cdn.parsers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for HAL+JSON content.
 *
 * @author Jens Borch Christiansen
 */
public interface HalJsonContent {

    String JSON_COMPOSITION = "composition";
    String JSON_COMPOSITION_TYPE = "composition-type";
    String JSON_CONTENT = "content";
    String JSON_HREF = "href";
    String JSON_LINKS = "_links";
    List<String> JSON_METADATA = Collections.unmodifiableList(Arrays.asList(new String[]{"seo"}));
    String JSON_REF = "ref";
    String JSON_SELF_LINK = "self";

}
