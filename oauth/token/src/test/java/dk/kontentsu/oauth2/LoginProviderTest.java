package dk.kontentsu.oauth2;

import dk.kontentsu.oauth2.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link LoginProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginProviderTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TestLoginProvider login;

    @Before
    public void setup() throws Exception {
        doNothing().when(request).login("username", "password");
    }

    @Test
    public void testLoginNoRoles() {
        User user = login.login("username", "password");
        assertNotNull(user);
        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void testLogin() {
        Set<Principal> principals = new HashSet<>();
        principals.add(new Principal() {
            @Override
            public String getName() {
                return "group";
            }
        });
        login.subject = new Subject(true, principals, new HashSet<>(0), new HashSet<>(0));
        User user = login.login("username", "password");
        assertNotNull(user);
        assertEquals(1, user.getRoles().size());
    }

    public static class TestLoginProvider extends LoginProvider {

        Subject subject = new Subject();

        @Override
        public Subject getSubject() throws PolicyContextException {
            return subject;
        }

    }

}
