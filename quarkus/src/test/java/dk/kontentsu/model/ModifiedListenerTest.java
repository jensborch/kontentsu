package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@Transactional
class ModifiedListenerTest {

    @Inject
    private EntityManager em;
    
    @Test
    void testModified() throws Exception {
        Item i = new Item(Term.create().append("test"), "default", new MimeType("text", "plain"));
        assertEquals(i.getCreated(), i.getModified());
        TimeUnit.SECONDS.sleep(1);
        em.persist(i);
        assertNotEquals(i.getCreated(), i.getModified());
    }
}
