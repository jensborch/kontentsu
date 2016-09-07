package dk.kontentsu.cdn.test;

import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

/**
 *
 * @author Jens Borch Christiansen
 */
public final class TestEJBContainer {

    private TestEJBContainer() {
    }

    public static EJBContainer create() throws NamingException {
        Properties props = new Properties();
        props.put("CdnDS", "new://Resource?type=DataSource");
        props.put("CdnDS.JdbcDriver", "org.h2.Driver");
        props.put("CdnDs.JdbcUrl", "jdbc:h2:mem:CdnDB;create=true");
        props.put("CdnDs.JtaManaged", "true");
        EJBContainer container = EJBContainer.createEJBContainer(props);
        return container;
    }

    public static void inject(final EJBContainer container, final Object obj) throws NamingException {
        container.getContext().bind("inject", obj);
    }

}
