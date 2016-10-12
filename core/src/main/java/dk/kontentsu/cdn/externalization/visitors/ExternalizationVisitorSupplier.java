package dk.kontentsu.cdn.externalization.visitors;

import java.util.Iterator;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kontentsu.cdn.model.internal.Version;

/**
 *
 * @author Jens Borch Christiansen
 */
@Singleton
public class ExternalizationVisitorSupplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalizationVisitorSupplier.class);

    @Inject
    private Instance<ExternalizationVisitor.Factory> factories;

    @PostConstruct
    private void init() {
        factories.forEach(f -> LOGGER.info("Found externalization visitor factory: {}", f.toString()));
    }


    public ExternalizationVisitor get(final Version version) {
        Iterator<ExternalizationVisitor.Factory> i = factories.iterator();
        while (i.hasNext()) {
            ExternalizationVisitor.Factory f = i.next();
            Optional<ExternalizationVisitor> v = f.create(version);
            if (v.isPresent()) {
                return v.get();
            }
        }
        return new DefaultExternalizationVisitor(version);
    }

}
