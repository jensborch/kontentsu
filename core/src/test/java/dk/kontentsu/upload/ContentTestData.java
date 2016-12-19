package dk.kontentsu.upload;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


/**
 * Class encapsulating test CDN content.
 *
 * @author Jens Borch Christiansen
 */
public class ContentTestData {

    private byte[] getDate(final String file) throws IOException {
        return getData(getStream(file));
    }

    private InputStream getStream(final String file) {
        return this.getClass().getClassLoader().getResourceAsStream(file);
    }

    public byte[] getArticle(final int index) throws IOException {
        switch (index) {
            case 1:
                return getDate("content/article1.json");
            case 2:
                return getDate("content/article2.json");
            default:
                return getDate("content/article1.json");
        }
    }

    public byte[] getContact() throws IOException {
        return getDate("content/contact.json");
    }

    public byte[] getFooter() throws IOException {
        return getDate("content/footer.json");
    }

    public byte[] getFullPage() throws IOException {
        return getDate("content/page-full.json");
    }

    public byte[] getSimplePage() throws IOException {
        return getDate("content/page-simple.json");
    }

    public String getSimplePageResults(final int index) {
        switch (index) {
            case 1:
                return getResults("content/page-simple-results1.json");
            case 2:
                return getResults("content/page-simple-results2.json");
            default:
                return getResults("content/page-simple-results1.json");
        }
    }

    public String getResults(final String file) {
        return new BufferedReader(new InputStreamReader(getStream(file), StandardCharsets.UTF_8))
                .lines()
                .parallel()
                .collect(Collectors.joining(""))
                .replaceAll("\\s\\s+", "");
    }

    private byte[] getData(final InputStream content) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = content.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

}
