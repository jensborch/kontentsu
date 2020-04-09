package dk.kontentsu.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.transaction.UserTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link Transaction}.
 *
 * @author Jens Borch Christiansen
 */
@ExtendWith(MockitoExtension.class)
public class TransactionTest {

    @Mock
    private UserTransaction transaction;

    @Test
    public void testApply() throws Exception {
        String result = Transaction.create(transaction).param("test").apply(s -> s);
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

}
