package dk.kontentsu.api.exposure;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.persistence.NoResultException;

import dk.kontentsu.test.TestEJBContainer;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link ExternalFileExposure}.
 *
 * @author Jens Borch Christiansen
 */
@Ignore
public class ExternalFileExposureIT {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private static EJBContainer container;

    @EJB
    private ExternalFileExposure exposure;

    @BeforeEachClass
    public static void setUpClass() {
        container = TestEJBContainer.create();
    }

    @AfterClass
    public static void tearDownClass() {
        if (container != null) {
            container.close();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        TestEJBContainer.inject(container, this);
    }

    @Test
    public void notFound() {
        catchException(exposure).get("test/notfound", null, null);
        assertTrue(caughtException() instanceof EJBException);
        assertTrue(caughtException().getCause().getCause() instanceof NoResultException);
    }
}
