package dk.kontentsu.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Class encapsulating test content for Kontentsu.
 *
 * @author Jens Borch Christiansen
 */
public class ContentTestData {

    private static final int BUFFER = 1024;

    private final Type type;

    public ContentTestData() {
        this.type = Type.HAL;
    }

    public ContentTestData(final Type type) {
        this.type = type;
    }

    private byte[] getDate(final String file) throws IOException {
        return getData(getStream(file));
    }

    private InputStream getStream(final String file) {
        return this.getClass().getClassLoader().getResourceAsStream(type.getPath() + file);
    }

    public byte[] getArticle(final int index) throws IOException {
        switch (index) {
            case 1:
                return getDate("article1.json");
            case 2:
                return getDate("article2.json");
            default:
                return getDate("article1.json");
        }
    }

    public byte[] getContact() throws IOException {
        return getDate("contact.json");
    }

    public byte[] getFooter() throws IOException {
        return getDate("footer.json");
    }

    public byte[] getFullPage() throws IOException {
        return getDate("page-full.json");
    }

    public byte[] getSimplePage() throws IOException {
        return getDate("page-simple.json");
    }

    public String getSimplePageResults(final int index) {
        switch (index) {
            case 1:
                return getResults("page-simple-results1.json");
            case 2:
                return getResults("page-simple-results2.json");
            default:
                return getResults("page-simple-results1.json");
        }
    }

    public String getResults(final String file) {
        String result = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getStream(file), StandardCharsets.UTF_8))) {
            result = reader.lines()
                    .parallel()
                    .collect(Collectors.joining(""))
                    .replaceAll("\\s\\s+", "");
        } catch (IOException e) {
            fail("Could not read test data: " + file);
        }
        return result;
    }

    private byte[] getData(final InputStream content) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER];
            int length;
            while ((length = content.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Different content types supported.
     */
    public enum Type {
        HAL, JSON;

        public String getPath() {
            return "content/" + name().toLowerCase() + "/";
        }
    }
}
