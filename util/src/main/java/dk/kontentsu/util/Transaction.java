package dk.kontentsu.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ejb.ApplicationException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fluent API for running code in a transaction.
 *
 * @author Jens Borch Christiansen
 */
public final class Transaction {

    private static final Logger LOGGER = LogManager.getLogger();

    private Transaction() {
    }

    public static ToCommit create(final UserTransaction transaction) {
        return new ToCommit(transaction);
    }

    /**
     * ToCommit the code im the given transaction.
     */
    public static final class ToCommit {

        private final UserTransaction userTransaction;

        private ToCommit(final UserTransaction transaction) {
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

        private final UserTransaction transaction;
        private final T param;

        private Parameter(final UserTransaction transaction, final T parameter) {
            if (transaction == null  || parameter == null) {
                throw new IllegalArgumentException("Transaction and parameter can't be null");
            }
            this.param = parameter;
            this.transaction = transaction;
        }

        public <R> R apply(final Function<T, R> function) throws Exception {
            R result;
            try {
                transaction.begin();
                result = function.apply(param);
                transaction.commit();
            } catch (Exception e) {
                if (rollback(e)) {
                    rollback(transaction);
                } else {
                    transaction.commit();
                }
                throw e;
            }
            return result;
        }

        public void accept(final Consumer<T> consumer) throws Exception {
            apply(p -> {
                consumer.accept(p);
                return null;
            });
        }

        private ApplicationException getApplicationException(Throwable e) {
            return getApplicationException(e.getClass());
        }

        private ApplicationException getApplicationException(Class<?> c) {
            return c.getAnnotation(ApplicationException.class);
        }

        private boolean isApplicationException(Class<?> c) {
            ApplicationException annotation = getApplicationException(c);
            return annotation != null && annotation.inherited();
        }

        private boolean rollback(Exception e) {
            return new Finder<>(this::isApplicationException, Class::getSuperclass)
                    .find(e.getClass())
                    .map(this::getApplicationException)
                    .map(ApplicationException::rollback)
                    .orElseGet(() -> Optional.ofNullable(getApplicationException(e))
                            .map(ApplicationException::rollback)
                            .orElse(e instanceof RuntimeException));
        }

        private void rollback(UserTransaction ut) {
            try {
                if (ut != null) {
                    ut.rollback();
                }
            } catch (IllegalStateException | SecurityException | SystemException ex) {
                LOGGER.error("Error performing transaction roll-back...", ex);
            }
        }
    }


}
