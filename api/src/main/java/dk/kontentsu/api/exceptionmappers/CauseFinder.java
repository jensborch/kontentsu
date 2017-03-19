
package dk.kontentsu.api.exceptionmappers;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Iterates through all causes to find exception matching predicate.
 */
public class CauseFinder {

    private final Predicate<Throwable> predicate;

    public CauseFinder(final Predicate<Throwable> predicate) {
        this.predicate = predicate;
    }

    public Optional<Throwable> findCause(final Throwable t) {
        Throwable cause = t.getCause();
        return cause == null ? Optional.empty()
                : predicate.test(cause) ? Optional.of(cause) : findCause(cause);
    }
}
