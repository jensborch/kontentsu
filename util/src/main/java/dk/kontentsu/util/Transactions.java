package dk.kontentsu.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.transaction.UserTransaction;

/**
 * Fluent API for running code in a transaction.
 *
 * @author Jens Borch Christiansen
 */
public final class Transactions {

    private Transactions() {
    }

    public static Commit commit(final UserTransaction transaction) {
        return new Commit(transaction);
    }

    /**
     * Commit the code im the given transaction.
     */
    public static final class Commit {

        private final UserTransaction userTransaction;

        private Commit(final UserTransaction transaction) {
            this.userTransaction = transaction;
        }

        public <T> Parameter<T> param(final T parameter) {
            return new Parameter<>(userTransaction, parameter);
        }

    }

    /**
     * Parameter for code to run in transaction.
     *
     * @param <T> the type of the parameter
     */
    public static final class Parameter<T> {

        private final UserTransaction userTransaction;
        private final T parameter;

        private Parameter(final UserTransaction transaction, final T parameter) {
            this.parameter = parameter;
            this.userTransaction = transaction;
        }

        public <R> R apply(final Function<T, R> function) throws Exception {
            try {
                userTransaction.begin();
                return function.apply(parameter);
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

}
