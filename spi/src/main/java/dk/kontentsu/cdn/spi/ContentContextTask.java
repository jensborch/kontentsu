package dk.kontentsu.cdn.spi;

/**
 * Run some code in the CDI content context.
 *
 * @author Jens Borch Christiansen
 */
@FunctionalInterface
public interface ContentContextTask {

    void run();

}
