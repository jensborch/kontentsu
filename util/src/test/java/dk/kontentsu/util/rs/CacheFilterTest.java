package dk.kontentsu.util.rs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link CacheFilter}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheFilterTest {

    @Mock
    private ContainerRequestContext request;

    @Mock
    private ContainerResponseContext response;

    @Test
    public void testNullFilter() throws Exception {
        CacheFilter filter = new CacheFilter(null, null, null);
        filter.filter(request, response);
        verify(response, times(0)).getHeaders();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws Exception {
        CachControl cc = mock(CachControl.class);
        when(cc.noCache()).thenReturn(Boolean.TRUE);
        MultivaluedMap map = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(map);
        CacheFilter filter = new CacheFilter(cc, null, null);
        filter.filter(request, response);
        verify(response, times(1)).getHeaders();
        verify(map, times(1)).putSingle(HttpHeaders.CACHE_CONTROL, "no-cache");
    }

}
