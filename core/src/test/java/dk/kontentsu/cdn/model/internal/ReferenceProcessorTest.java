package dk.kontentsu.cdn.model.internal;

import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.model.internal.ReferenceType;
import dk.kontentsu.cdn.model.internal.ReferenceProcessor;
import dk.kontentsu.cdn.model.internal.Version;
import dk.kontentsu.cdn.model.internal.TemporalReferenceTree;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.upload.ContentTestData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ReferenceProcessor}
 *
 * @author Jens Borch Christiansen
 */
public class ReferenceProcessorTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

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
                .composition(article1, ReferenceType.COMPOSITION)
                .composition(article2, ReferenceType.COMPOSITION)
                .composition(contact, ReferenceType.COMPOSITION)
                .build();
        page.addVersion(pageVersion);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testVisitor() {
        TestVisitor visitor = new TestVisitor();
        ReferenceProcessor<TestVisitor> processor = new ReferenceProcessor<>(pageVersion, visitor);
        List<TemporalReferenceTree<TestVisitor>> result = processor.process();
        assertEquals(2, result.size());
        assertEquals(3, ((TestVisitor) result.get(0).getVisitor()).names.size());

        assertArrayEquals(new String[]{"page-simple", "contact", "article2"}, ((TestVisitor) result.get(0).getVisitor()).names.toArray(new String[2]));

        assertEquals(3, ((TestVisitor) result.get(1).getVisitor()).names.size());

        assertArrayEquals(new String[]{"page-simple", "contact", "article2"}, ((TestVisitor) result.get(1).getVisitor()).names.toArray(new String[2]));

        assertNotEquals(result.get(0), result.get(1));
        assertNotEquals(result.get(0).getInteval(), result.get(1).getInteval());
        assertEquals(new Interval(NOW, NOW.plusDays(10)), result.get(0).getInteval());
        assertEquals(new Interval(NOW.plusDays(15)), result.get(1).getInteval());
    }

    private class TestVisitor implements TemporalReferenceTree.Visitor {

        List<String> names = new ArrayList<>();

        @Override
        public TestVisitor copy() {
            TestVisitor copy = new TestVisitor();
            copy.names = new ArrayList<>();
            return copy;
        }

        @Override
        public void visit(TemporalReferenceTree.Node node) {
            names.add(node.getVersion().getItem().getName());
        }

    }

}
