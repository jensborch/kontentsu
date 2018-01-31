package dk.kontentsu.externalization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.util.DelTreeFileVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link ScheduledExternalizerService}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledExternalizerServiceTest {

    @Mock
    private ExternalFileRepository fileRepo;

    @Mock
    private Timer timer;

    @InjectMocks
    private ScheduledExternalizerService service;

    private Path path;

    @Before
    public void setUp() throws Exception {
        path = Files.createTempDirectory("junit");
        Files.createFile(path.resolve("delete"));
        Host host = new Host("test", "desc", path.toString());
        Term path = Term.parse("uri:/test/file/");
        Item item = new Item(path, "xl", MimeType.APPLICATION_JSON_TYPE);
        item.addHost(host);
        Content content = new Content("{ \"this\": \"is a test\"}".getBytes(), Charset.defaultCharset());
        ExternalFile file = ExternalFile.builder()
                .from(ZonedDateTime.now())
                .content(content)
                .item(item)
                .build();
        List<ExternalFile> list = new ArrayList<>(1);
        list.add(file);
        when(fileRepo.findAll(any(ZonedDateTime.class))).thenReturn(list);
        when(timer.getSchedule()).thenReturn(new ScheduleExpression().year(2016).month(10).dayOfMonth(12).hour(10).minute(5).second(42));
    }

    @After
    public void tearDown() throws Exception {
        Files.walkFileTree(path, new DelTreeFileVisitor());
    }

    @Test
    public void testExecute() {
        service.execute(timer);
        assertTrue(path.resolve("test/file/file-xl.json").toFile().exists());
        assertFalse(path.resolve("delete").toFile().exists());
    }

}
