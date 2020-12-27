package dk.kontentsu.model;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Hibernate listener for updating last modified.
 *
 * @author Jens Borch Christiansen
 */
public class ModifiedListener implements PostUpdateEventListener {

	private static final long serialVersionUID = 1488530099364655588L;

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (event.getEntity() instanceof AbstractBaseEntity) {
			((AbstractBaseEntity) event.getEntity()).updateModified();
		}
	}

}