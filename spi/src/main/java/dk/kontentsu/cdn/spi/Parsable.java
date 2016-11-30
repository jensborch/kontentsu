package dk.kontentsu.cdn.spi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for CDN content that can be processed by a parser - e.g. a CDI bean
 * using the {@link ContentScoped} scope.
 *
 * @author Jens Borch Christiansen
 */
public interface Parsable {

    UUID getUuid();

    Optional<Charset> getEncoding();

    InputStream getDataAsBinaryStream();

    String getData();

    MimeType getMimeType();

}
