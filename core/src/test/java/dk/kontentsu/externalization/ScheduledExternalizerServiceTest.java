package dk.kontentsu.externalization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kontentsu.model.Content;
import dk.kontentsu.model.ExternalFile;
import dk.kontentsu.model.Node;
import dk.kontentsu.model.Item;
import dk.kontentsu.model.MimeType;
import dk.kontentsu.model.Term;
import dk.kontentsu.repository.ExternalFileRepository;
import dk.kontentsu.util.DelTreeFileVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

/**
 * Test for {@link ScheduledExternalizerService}.
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
public class ScheduledExternalizerServiceTest {

    @Mock
    private ExternalFileRepository fileRepo;

    @Mock
    private Scheduler timer;

    @Mock
    private JobExecutionContext context;

    @InjectMocks
    private ScheduledExternalizerService service;

    private Path path;

    @BeforeEach
    public void setUp() throws Exception {
        path = Files.createTempDirectory("junit");
        Files.createFile(path.resolve("delete"));
        Node host = new Node("test", "desc", path.toString());
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
        when(context.getFireTime()).thenReturn(new Date());
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walkFileTree(path, new DelTreeFileVisitor());
    }

    @Test
    public void testExecute() {
        service.execute(context);
        assertTrue(path.resolve("test/file/file-xl.json").toFile().exists());
        assertFalse(path.resolve("delete").toFile().exists());
    }

}
