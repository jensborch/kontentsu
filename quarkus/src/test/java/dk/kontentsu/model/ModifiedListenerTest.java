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

    private Node node;

    @BeforeEach
    void setup() throws Exception {
        node = new Node("name", "desc", new URI("ftp://test"), "test");
        em.persist(node);
    }

    @AfterEach
    void tearDown() throws Exception {
        Node n = em.find(Node.class, node.getId());
        em.remove(n);
    }

    @Test
    void testNotModified() throws Exception {
        Node n = em.find(Node.class, node.getId());
        assertEquals(n.getCreated(), n.getModified());
    }

    @Test
    void testModified() throws Exception {
        Node n = em.find(Node.class, node.getId());
        TimeUnit.SECONDS.sleep(1);
        n.setDescription("test");
        em.flush();
        assertNotEquals(n.getCreated(), n.getModified());
    }
}
