package dk.kontentsu.spi;

/**
 *
 */
public class ContentProcessingContextManager {

    private final static ContentProcessingContextManager INSTANCE = new ContentProcessingContextManager();

    //private final ThreadLocal<StartableContentContext> context = new ThreadLocal<>();
    private StartableContentContext context;

    public static ContentProcessingContextManager getInstance() {
        return INSTANCE;
    }

    public void register(StartableContentContext context) {
        this.context = context;
    }

    public StartableContentContext context() {
        return context;
    }

}
