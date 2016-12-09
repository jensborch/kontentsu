package dk.kontentsu.cdn.model.internal;

import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.Interval;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.SemanticUriPath;
import dk.kontentsu.cdn.spi.MimeType;
import dk.kontentsu.cdn.upload.ContentTestData;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
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
        TestVisitor visitor = new TestVisitor();
        ReferenceProcessor<TestVisitor> processor = new ReferenceProcessor<>(pageVersion, visitor);
        List<TemporalReferenceTree<TestVisitor>> result = processor.process();
        assertEquals(2, result.size());
        //TODO: Fix
        //assertEquals(3, ((TestVisitor) result.get(0).getVisitor()).names.size());

        //assertArrayEquals(new String[]{"page-simple", "contact", "article2"}, ((TestVisitor) result.get(0).getVisitor()).names.toArray(new String[2]));

        //assertEquals(3, ((TestVisitor) result.get(1).getVisitor()).names.size());

        //assertArrayEquals(new String[]{"page-simple", "contact", "article2"}, ((TestVisitor) result.get(1).getVisitor()).names.toArray(new String[2]));

        assertNotEquals(result.get(0), result.get(1));
        assertNotEquals(result.get(0).getInteval(), result.get(1).getInteval());
        assertTrue(result.stream().filter(n -> n.getInteval().equals(new Interval(NOW, NOW.plusDays(10)))).findAny().isPresent());
        assertTrue(result.stream().filter(n -> n.getInteval().equals(new Interval(NOW.plusDays(15)))).findAny().isPresent());
    }

    private class TestVisitor implements TemporalReferenceTree.Visitor {

        List<String> names = new ArrayList<>();


        @Override
        public void visit(TemporalReferenceTree.Node node) {
            names.add(node.getVersion().getItem().getName());
        }

    }

}
