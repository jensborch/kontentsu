package dk.kontentsu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@Transactional
class ModifiedListenerTest {

    @Inject
    private EntityManager em;

    private Host host;

    @BeforeEach
    void setup() throws Exception {
        host = new Host("name", "desc", new URI("ftp://test"), "test");
        em.persist(host);
    }

    @AfterEach
    void tearDown() throws Exception {
        Host n = em.find(Host.class, host.getId());
        em.remove(n);
    }

    @Test
    void testNotModified() throws Exception {
        Host n = em.find(Host.class, host.getId());
        assertEquals(n.getCreated(), n.getModified());
    }

    @Test
    void testModified() throws Exception {
        Host n = em.find(Host.class, host.getId());
        TimeUnit.SECONDS.sleep(1);
        n.setDescription("test");
        em.flush();
        assertNotEquals(n.getCreated(), n.getModified());
    }
}
