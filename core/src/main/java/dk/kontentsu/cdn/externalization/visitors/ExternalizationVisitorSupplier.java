package dk.kontentsu.cdn.externalization.visitors;

import java.util.Iterator;
import java.util.Optional;

import javax.ejb.Singleton;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import dk.kontentsu.cdn.model.internal.Version;

/**
 *
 * @author Jens Borch Christiansen
 */
@Singleton
public class ExternalizationVisitorSupplier {

    @Inject
    private Instance<ExternalizationVisitor.Factory> factories;

    public ExternalizationVisitor get(final Version version) {
        Iterator<ExternalizationVisitor.Factory> i = factories.iterator();
        while (i.hasNext()) {
            Optional<ExternalizationVisitor> v = i.next().create(version);
            if (v.isPresent()) {
                return v.get();
            }
        }
        return new DefaultExternalizationVisitor(version);
    }

}
