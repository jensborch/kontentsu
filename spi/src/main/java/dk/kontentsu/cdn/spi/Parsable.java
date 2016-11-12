package dk.kontentsu.cdn.spi;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 *
 * @author Jens Borch Christiansen
 */
public interface Parsable {

    Optional<Charset> getEncoding();

    byte[] getDataAsBytes();

    String getData();

}
