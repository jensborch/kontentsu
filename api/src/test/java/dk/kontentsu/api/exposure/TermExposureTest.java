package dk.kontentsu.api.exposure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.api.exceptionmappers.NoResultExceptionMapper;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.TermRepository;

/**
 * Test for {@link TermExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class TermExposureTest extends JerseyTest {

    @Mock
    private TermRepository termRepo;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(TermExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(NoResultExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(termRepo).to(TermRepository.class);
                    }
                });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        List<Term> root = new ArrayList<>();
        Term t = Term.parse("uri:/test1/test2/");
        root.add(t.getParent().get().getParent().get());
        when(termRepo.findAll()).thenReturn(root);
        when(termRepo.get("uri:/test1/test2/")).thenReturn(t);
    }

    @Test
    public void testFindTaxonomies() {
        given().get(target("terms").getUri())
                .then()
                .statusCode(200)
                .body("[0].rel", is("taxonomy"));
    }

    @Test
    public void testUnknownTaxonomy() {
        given().get(target("terms/unknown").getUri())
                .then()
                .statusCode(404);
    }

    @Test
    public void testFindTaxonomy() {
        given().get(target("terms/uri").getUri())
                .then()
                .statusCode(200);
    }

    @Test
    public void testFindTerms() {
        given().get(target("terms/uri/test1/test2").getUri())
                .then()
                .statusCode(200)
                .body("path", is("uri:/test1/test2/"))
                .body("taxonomy.rel", is("taxonomy"));
    }

}
