package dk.kontentsu.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.transaction.UserTransaction;

/**
 * Transaction util.
 *
 * @author Jens Borch Christiansen
 */
public class Transactions {

    private UserTransaction userTransaction;

    public Transactions(final UserTransaction transaction) {
        this.userTransaction = transaction;
    }

    public <T, R> R apply(final Function<T, R> function, final T param) throws Exception {
        try {
            userTransaction.begin();
            return function.apply(param);
        } finally {
            userTransaction.commit();
        }
    }

    public <T> void accept(final Consumer<T> consumer, final T param) throws Exception {
        try {
            userTransaction.begin();
            consumer.accept(param);
        } finally {
            userTransaction.commit();
        }
    }

}
