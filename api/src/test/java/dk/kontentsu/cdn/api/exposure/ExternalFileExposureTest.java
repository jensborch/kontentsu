package dk.kontentsu.cdn.api.exposure;

import dk.kontentsu.cdn.api.exposure.ExternalFileExposure;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import dk.kontentsu.cdn.api.exceptionmappers.ConstraintViolationExceptionMapper;
import dk.kontentsu.cdn.api.exceptionmappers.ContainerExceptionMapper;
import dk.kontentsu.cdn.model.Content;
import dk.kontentsu.cdn.model.ExternalFile;
import dk.kontentsu.cdn.model.MimeType;
import dk.kontentsu.cdn.model.SemanticUri;
import dk.kontentsu.cdn.model.internal.Item;
import dk.kontentsu.cdn.repository.ExternalFileRepository;

/**
 * Test for {@link ExternalFileExposure}.
 *
 * @author Jens Borch Christiansen
 */
public class ExternalFileExposureTest extends JerseyTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Mock
    private ExternalFileRepository repo;

    @Override
    protected Application configure() {
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
        Content content = new Content("{\"test\": \"test\"}".getBytes(), StandardCharsets.UTF_8, MimeType.APPLICATION_JSON_TYPE);
        ExternalFile file = ExternalFile.builder()
                .content(content)
                .item(new Item(SemanticUri.parse("test/test")))
                .from(NOW)
                .build();
        Mockito.when(repo.getByUri(Mockito.eq(SemanticUri.parse("test/test")), Mockito.any(ZonedDateTime.class))).thenReturn(file);
        Mockito.when(repo.getByUri(Mockito.eq(SemanticUri.parse("test/not-found")), Mockito.any(ZonedDateTime.class))).thenThrow(new EJBException(new NoResultException("test")));
    }

    @Test
    public void testGetContent() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("files/test/test").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("test", equalTo("test"))
                .statusCode(200);
    }

    @Test
    public void testNotFound() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("files/test/not-found").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("code", equalTo("not-found-error"))
                .statusCode(404);
    }

    @Test
    public void testWrongDateTimeFormat() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("files/test/test").queryParam("at", "not-a-date-time").getUri())
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .body("code", equalTo("validation-error"))
                .statusCode(400);
    }

    @Test
    public void testDateTimeFormat() throws Exception {
        given().when()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .get(target("files/test/test").queryParam("at", "2010-12-24T20:00:00Z").getUri().toString().replace("%3A", ":"))
                .then()
                .contentType(MimeType.APPLICATION_JSON_TYPE.toString())
                .statusCode(200);

        LocalDateTime local = LocalDateTime.of(2010, Month.DECEMBER, 24, 20, 0);
        ZonedDateTime time = ZonedDateTime.of(local, ZoneOffset.UTC);
        Mockito.verify(repo, Mockito.times(1)).getByUri(SemanticUri.parse("test/test"), time);
    }
}
