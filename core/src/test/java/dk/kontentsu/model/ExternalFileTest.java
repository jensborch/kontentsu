/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kontentsu.model;

import dk.kontentsu.model.SemanticUri;
import dk.kontentsu.model.ExternalFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import dk.kontentsu.model.internal.Item;
import dk.kontentsu.model.internal.Version;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        item = new Item(SemanticUri.parse("hello/hello"));
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

    @After
    public void tearDown() {
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
