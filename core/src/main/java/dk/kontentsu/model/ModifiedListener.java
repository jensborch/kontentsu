package dk.kontentsu.model;

import javax.persistence.PrePersist;

/**
 * JPA entity listener for updating last modified.
 *
 * @author Jens Borch Christiansen
 */
public class ModifiedListener {
    
    @PrePersist
	public void prePersist(AbstractBaseEntity entity) {
		entity.updateModified();
	}
}