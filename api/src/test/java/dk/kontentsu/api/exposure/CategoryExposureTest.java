package dk.kontentsu.api.exposure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Application;

import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link CategoryExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class CategoryExposureTest extends JerseyTest {

    //TODO: Fix...
    /*@Mock
    private CategoryRepository catRepo;

    @Mock
    private TaxonomyRepository taxRepo;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(CategoryExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(catRepo).to(CategoryRepository.class);
                        bind(taxRepo).to(TaxonomyRepository.class);
                    }
                });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<Taxonomy> taxonomies = new ArrayList<>();
        Taxonomy t = new Taxonomy("taxonomy1");
        taxonomies.add(t);
        when(taxRepo.findAll()).thenReturn(taxonomies);
        when(taxRepo.getByName("taxonomy1")).thenReturn(t);
        when(taxRepo.getByName("unknown")).thenThrow(new EJBException(new NoResultException("test")));

        List<Category> categories = new ArrayList<>();
        Taxon c = Taxon.parse(t, "test1/test2");
        categories.add(c);
        when(catRepo.getByTaxonomy(t)).thenReturn(categories);
        when(catRepo.findAll()).thenReturn(categories);
        when(catRepo.getByTaxonomy(t, "test1/test2")).thenReturn(c);
    }

    @Test
    public void testFindTaxonomies() throws Exception {
        given().get(target("categories").getUri())
                .then()
                .statusCode(200)
                .body("[0].rel", is("taxonomy"));
    }

    @Test
    public void testUnknownTaxonomy() throws Exception {
        given().get(target("categories/unknown").getUri())
                .then()
                .statusCode(404);
    }

    @Test
    public void testFindTaxonomy() throws Exception {
        given().get(target("categories/taxonomy1").getUri())
                .then()
                .statusCode(200);
    }

    @Test
    public void testFindCategories() throws Exception {
        given().get(target("categories/taxonomy1/test1/test2").getUri())
                .then()
                .statusCode(200)
                .body("path", is("test1/test2"))
                .body("taxonomy.rel", is("taxonomy"));
    }*/

}
