package dk.kontentsu.util.rs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    public void testNullFilter() {
        CacheFilter filter = new CacheFilter((Cache) null);
        filter.filter(request, response);
        verify(response, times(0)).getHeaders();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() {
        Cache cc = mock(Cache.class);
        when(cc.maxAge()).thenReturn(40);
        when(cc.sMaxAge()).thenReturn(-1);
        when(cc.isPrivate()).thenReturn(Boolean.FALSE);
        when(cc.mustRevalidate()).thenReturn(Boolean.FALSE);
        when(cc.noStore()).thenReturn(Boolean.TRUE);
        when(cc.noTransform()).thenReturn(Boolean.FALSE);
        when(cc.proxyRevalidate()).thenReturn(Boolean.FALSE);
        when(cc.unit()).thenReturn(TimeUnit.SECONDS);
        MultivaluedMap map = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(map);
        CacheFilter filter = new CacheFilter(cc);
        filter.filter(request, response);
        verify(response, times(1)).getHeaders();
        verify(map, times(1)).putSingle(HttpHeaders.CACHE_CONTROL, "no-store, max-age=40");
    }

}
