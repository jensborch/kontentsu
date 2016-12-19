package dk.kontentsu.cdn.model.internal;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.upload.ContentTestData;
import dk.kontentsu.scope.InjectableContentProcessingScope;
import dk.kontentsu.spi.ContentProcessingExtension;
import dk.kontentsu.spi.MimeType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import javax.inject.Inject;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ReferenceProcessor}
 *
 * @author Jens Borch Christiansen
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({TestVisitor.class, ContentProcessingExtension.class})
public class ReferenceProcessorTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Inject
    private TestVisitor visitor;

    private ContentTestData data;
    private Item page;
    private Version pageVersion;

    @Before
    public void setUp() throws Exception {
        data = new ContentTestData();
        SemanticUri semanticUri1 = new SemanticUri(new SemanticUriPath("items", "article2"), "article2");
        Item article1 = new Item(semanticUri1);
        Version articleVersion1 = Version.builder()
                .content(new Content(data.getArticle(1), StandardCharsets.UTF_8, new MimeType("application", "hal+json")))
                .from(ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault()))
                .to(NOW.plusDays(10))
                .build();
        article1.addVersion(articleVersion1);

        SemanticUri semanticUri2 = new SemanticUri(new SemanticUriPath("items", "article2"), "article2");
        Item article2 = new Item(semanticUri2);
        Version articleVersion2 = Version.builder()
                .content(new Content(data.getArticle(2), StandardCharsets.UTF_8, new MimeType("application", "hal+json")))
                .from(NOW.plusDays(15))
                .build();
        article2.addVersion(articleVersion2);

        SemanticUri semanticUriContact = new SemanticUri(new SemanticUriPath("items", "contact"), "contact");
        Item contact = new Item(semanticUriContact);
        Version contactVersion = Version.builder()
                .content(new Content(data.getContact(), StandardCharsets.UTF_8, new MimeType("application", "hal+json")))
                .from(NOW)
                .build();
        contact.addVersion(contactVersion);

        SemanticUri semanticUriPage = new SemanticUri(new SemanticUriPath("pages", "page-simple"), "page-simple");
        page = new Item(semanticUriPage);
        pageVersion = Version.builder()
                .from(NOW)
                .content(new Content(data.getSimplePage(), StandardCharsets.UTF_8, new MimeType("application", "hal+json")))
                .reference(article1, ReferenceType.COMPOSITION)
                .reference(article2, ReferenceType.COMPOSITION)
                .reference(contact, ReferenceType.COMPOSITION)
                .build();
        page.addVersion(pageVersion);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testVisitor() {
        InjectableContentProcessingScope.execute(() -> {
            ReferenceProcessor<TestVisitor.TestResults, TestVisitor> processor = new ReferenceProcessor<>(pageVersion, visitor);
            List<TemporalReferenceTree<TestVisitor.TestResults, TestVisitor>> result = processor.process();

            assertEquals(2, result.size());
            assertEquals(2, result.get(0).getResult().names.size());
            assertArrayEquals(new String[]{"contact", "article2"}, result.get(0).getResult().names.toArray(new String[2]));
            assertEquals(2, result.get(1).getResult().names.size());
            assertArrayEquals(new String[]{"contact", "article2"}, result.get(1).getResult().names.toArray(new String[2]));
            assertNotEquals(result.get(0), result.get(1));
            assertNotEquals(result.get(0).getInteval(), result.get(1).getInteval());
            assertTrue(result.stream().filter(n -> n.getInteval().equals(new Interval(NOW, NOW.plusDays(10)))).findAny().isPresent());
            assertTrue(result.stream().filter(n -> n.getInteval().equals(new Interval(NOW.plusDays(15)))).findAny().isPresent());
        });
    }

}
