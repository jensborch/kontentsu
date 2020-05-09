package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javax.inject.Inject;

import dk.kontentsu.spi.InjectableContentProcessingScope;
import dk.kontentsu.model.processing.ReferenceProcessor;
import dk.kontentsu.model.processing.TemporalReferenceTree;
import dk.kontentsu.spi.ContentProcessingExtension;
import dk.kontentsu.spi.ContentProducer;
import dk.kontentsu.test.ContentTestData;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ReferenceProcessor}
 *
 * @author Jens Borch Christiansen
 */
@EnableAutoWeld
@AddPackages({TestVisitor.class, Content.class, ContentProducer.class})
@AddExtensions(ContentProcessingExtension.class)
public class ReferenceProcessorTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Inject
    private InjectableContentProcessingScope scope;

    @Inject
    private TestVisitor visitor;

    private Item page;
    private Version pageVersion;

    @BeforeEach
    public void setUp() throws Exception {
        ContentTestData data = new ContentTestData();
        Term path = Term.create().append("items").append("article2");
        Item article1 = new Item(path, MimeType.APPLICATION_JSON_TYPE);
        Version articleVersion1 = Version.builder()
                .content(new Content(data.getArticle(1), StandardCharsets.UTF_8))
                .from(ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault()))
                .to(NOW.plusDays(10))
                .build();
        article1.addVersion(articleVersion1);

        Item article2 = new Item(path, MimeType.APPLICATION_JSON_TYPE);
        Version articleVersion2 = Version.builder()
                .content(new Content(data.getArticle(2), StandardCharsets.UTF_8))
                .from(NOW.plusDays(15))
                .build();
        article2.addVersion(articleVersion2);

        Term contactPath = Term.create().append("items").append("contact");
        Item contact = new Item(contactPath, MimeType.APPLICATION_JSON_TYPE);
        Version contactVersion = Version.builder()
                .content(new Content(data.getContact(), StandardCharsets.UTF_8))
                .from(NOW)
                .build();
        contact.addVersion(contactVersion);

        Term pagePath = Term.create().append("items").append("page-simple");
        page = new Item(pagePath, MimeType.APPLICATION_JSON_TYPE);
        pageVersion = Version.builder()
                .from(NOW)
                .content(new Content(data.getSimplePage(), StandardCharsets.UTF_8))
                .reference(article1, ReferenceType.COMPOSITION)
                .reference(article2, ReferenceType.COMPOSITION)
                .reference(contact, ReferenceType.COMPOSITION)
                .build();
        page.addVersion(pageVersion);
    }

    @Test
    public void testVisitor() {
        scope.execute(() -> {
            ReferenceProcessor<TestVisitor.TestResults, TestVisitor> processor = new ReferenceProcessor<>(pageVersion, visitor);
            List<TemporalReferenceTree<TestVisitor.TestResults, TestVisitor>> result = processor.process();

            assertEquals(2, result.size());
            assertEquals(2, result.get(0).getResult().names.size());
            assertTrue(result.get(0).getResult().names.contains("contact.json"));
            assertTrue(result.get(0).getResult().names.contains("article2.json"));
            assertEquals(2, result.get(1).getResult().names.size());
            assertTrue(result.get(1).getResult().names.contains("contact.json"));
            assertTrue(result.get(1).getResult().names.contains("article2.json"));
            assertNotEquals(result.get(0), result.get(1));
            assertNotEquals(result.get(0).getInterval(), result.get(1).getInterval());
            assertTrue(result.stream().anyMatch(n -> n.getInterval().equals(new Interval(NOW, NOW.plusDays(10)))));
            assertTrue(result.stream().anyMatch(n -> n.getInterval().equals(new Interval(NOW.plusDays(15)))));
        });
    }

}
