package org.nightlabs.jfire.entityuserset.notification;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.notification.id.EntityUserSetNotificationFilterEntryID"
 *		detachable="true"
 *		table="JFireEntityUserSet_EntityUserSetNotificationFilterEntry"
 *
 * @jdo.create-objectid-class field-order="organisationID, entityUserSetNotificationFilterEntryID"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 */
public class EntityUserSetNotificationFilterEntry
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String entityUserSetNotificationFilterEntryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String entityClassName;

	protected EntityUserSetNotificationFilterEntry() { }

	public EntityUserSetNotificationFilterEntry(String organisationID, String entityUserSetNotificationFilterEntryID, Class<?> entityClass) {
		this.organisationID = organisationID;
		this.entityUserSetNotificationFilterEntryID = entityUserSetNotificationFilterEntryID;

		this.entityClassName = entityClass.getName();
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public String getEntityUserSetNotificationFilterEntryID() {
		return entityUserSetNotificationFilterEntryID;
	}
	public String getEntityClassName() {
		return entityClassName;
	}

	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs) {
		return dirtyObjectIDs;
	}
}
