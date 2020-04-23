package dk.kontentsu.spi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public interface ScopedContent {

    String getData();

    InputStream getDataAsBinaryStream();

    Optional<Charset> getEncoding();

    String getHash();

    Integer getId();

    int getSize();

    UUID getUuid();

}
