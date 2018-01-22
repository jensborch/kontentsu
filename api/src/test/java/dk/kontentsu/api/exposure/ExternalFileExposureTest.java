package dk.kontentsu.api.exposure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Application;

import dk.kontentsu.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.ExternalFileRepository;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link ExternalFileExposure}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalFileExposureTest extends JerseyTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Mock
    private ExternalFileRepository repo;

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        MockitoAnnotations.initMocks(this);

        return new ResourceConfig()
                .register(ExternalFileExposure.class)
                .register(ContainerExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(repo).to(ExternalFileRepository.class);
                    }
                });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Content content = new Content("{\"test\": \"test\"}".getBytes(), StandardCharsets.UTF_8);
        ExternalFile file = ExternalFile.builder()
                .content(content)
                .item(new Item(new Term().append("test").append("test"), MimeType.APPLICATION_JSON_TYPE))
                .from(NOW)
                .build();
        Mockito.when(repo.getByUri(eq("test/test"), any(ZonedDateTime.class))).thenReturn(file);
        Mockito.when(repo.getByUri(eq("test/test"), eq(null))).thenReturn(file);
        Mockito.when(repo.getByUri(eq("test/not-found/"), eq(null))).thenThrow(new EJBException(new NoResultException("test")));
    }

    @Test
    public void testGetContent() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("/files/test/test").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("test", equalTo("test"))
                .statusCode(200);
    }

    @Test
    public void testNotFound() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("/files/test/not-found/").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("code", equalTo("not-found-error"))
                .statusCode(404);
    }

    @Test
    public void testWrongDateTimeFormat() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("/files/test/test").queryParam("at", "not-a-date-time").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("code", equalTo("validation-error"))
                .statusCode(400);
    }

    @Test
    public void testDateTimeFormat() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("/files/test/test").queryParam("at", "2010-12-24T20:00:00Z").getUri().toString().replace("%3A", ":"))
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .statusCode(200);

        LocalDateTime local = LocalDateTime.of(2010, Month.DECEMBER, 24, 20, 0);
        ZonedDateTime time = ZonedDateTime.of(local, ZoneOffset.UTC);
        Mockito.verify(repo, Mockito.times(1)).getByUri("test/test", time);
    }
}
