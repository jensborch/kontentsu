/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ExternalFile}.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalFileTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private ExternalFile file;
    private Version version;
    private Item item;

    @BeforeEach
    public void setUp() {
        item = new Item(Term.parse("uri:/hello/hello/"), MimeType.APPLICATION_JSON_TYPE);
        version = Version.builder()
                .from(NOW)
                .build();
        item.addVersion(version);
        file = ExternalFile.builder()
                .externalizationId("42")
                .item(item)
                .from(NOW)
                .build();
    }

    @Test
    public void testIsDifferent() {
        assertTrue(file.isDifferent(version));
    }

    @Test
    public void testNullId() {
        file = ExternalFile.builder()
                .item(item)
                .from(NOW)
                .build();
        assertTrue(file.isDifferent(version));
    }

    @Test
    public void testIsTheSame() {
        version.addExternalizationId("42");
        assertFalse(file.isDifferent(version));
    }
}
