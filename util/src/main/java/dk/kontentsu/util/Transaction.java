/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fluent API for running code in a transaction.
 *
 * @author Jens Borch Christiansen
 */
public final class Transaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

    private Transaction() {
    }

    public static ToCommit create(final UserTransaction transaction) {
        return new ToCommit(transaction);
    }

    /**
     * ToCommit the code in the given transaction.
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
            if (transaction == null || parameter == null) {
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

        private boolean rollback(final Exception e) {
            return e instanceof RuntimeException;
        }

        private void rollback(final UserTransaction ut) {
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
