package dk.kontentsu.api.exposure;

import dk.kontentsu.api.exposure.ExternalFileExposure;
import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import dk.kontentsu.test.TestEJBContainer;
import java.time.LocalDateTime;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.persistence.NoResultException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for {@link ExternalFileExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalFileExposureIT {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private static EJBContainer container;

    @EJB
    private ExternalFileExposure exposure;

    @BeforeClass
    public static void setUpClass() throws Exception {
        container = TestEJBContainer.create();
    }

    @AfterClass
    public static void tearDownClass() {
        if (container != null) {
            container.close();
        }
    }

    @Before
    public void setUp() throws Exception {
        TestEJBContainer.inject(container, this);
    }

    @Test
    public void notFound() throws Exception {
        catchException(exposure).get("test/notfound", null, null);
        assertTrue(caughtException() instanceof EJBException);
        assertTrue(caughtException().getCause().getCause() instanceof NoResultException);
    }
}
