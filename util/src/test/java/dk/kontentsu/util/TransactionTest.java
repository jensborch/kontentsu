package dk.kontentsu.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ejb.ApplicationException;
import javax.transaction.UserTransaction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link Transaction}.
 *
 * @author Jens Borch Christiansen
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionTest {

    @Mock
    private UserTransaction transaction;

    @Test
    public void testApply() throws Exception {
        String result = Transaction.create(transaction).param("test").apply(s -> {
            return s;
        });
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).commit();
        assertEquals("test", result);
    }

    @Test
    public void testException() throws Exception {
        RuntimeException toThrow = new RuntimeException("test");
        try {
            Transaction.create(transaction).param("test").apply(s -> {
                throw toThrow;
            });
        } catch (Exception ex) {
            assertEquals(toThrow, ex);
        }
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).rollback();
    }

    @Test
    public void testAppException() throws Exception {
        RuntimeException toThrow = new TestException();
        try {
            Transaction.create(transaction).param("test").apply(s -> {
                throw toThrow;
            });
        } catch (Exception ex) {
            assertEquals(toThrow, ex);
        }
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).commit();
    }

    @ApplicationException(inherited = true, rollback = false)
    private static class SuperException extends RuntimeException {

    }

    private static class TestException extends SuperException {

    }

}
