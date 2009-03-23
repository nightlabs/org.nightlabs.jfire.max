package org.nightlabs.jfire.entityuserset;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.util.Util;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.id.EntityRefID"
 *		detachable="true"
 *		table="JFireEntityUserSet_EntityRef"
 *
 * @jdo.inheritance strategy = "new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="entityUserSetOrganisationID, entityClassName, entityUserSetID, authorizedObjectID, entityObjectIDString"
 *
 * @jdo.fetch-group name="AuthorizedObjectRef.entityRefs" fields="entityUserSet, authorizedObjectRef"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="entityUserSet, authorizedObjectRef"
 */
public abstract class EntityRef<Entity>
implements Serializable
{
	private static final long serialVersionUID = 2L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String entityUserSetOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String entityClassName;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String entityUserSetID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="255"
	 */
	private String authorizedObjectID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="255"
	 */
	private String entityObjectIDString;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private EntityUserSet<Entity> entityUserSet;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private AuthorizedObjectRef<Entity> authorizedObjectRef;

	/**
	 * The number of both direct and indirect references to this <code>EntityRef</code>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int referenceCount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean directlyReferenced;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EntityRef() { }

	public EntityRef(AuthorizedObjectRef<Entity> authorizedObjectRef, Entity entity) {
		this.entityUserSet = authorizedObjectRef.getEntityUserSet();

		this.entityUserSetOrganisationID = entityUserSet.getOrganisationID();
		this.entityClassName = entityUserSet.getEntityClassName();
		this.entityUserSetID = entityUserSet.getEntityUserSetID();
		this.entityObjectIDString = entityUserSet.getEntityObjectIDString(entity);

		this.authorizedObjectRef = authorizedObjectRef;
		this.authorizedObjectID = authorizedObjectRef.getAuthorizedObjectID();

		this.setEntity(entity);
	}

	protected abstract void setEntity(Entity entity);

	public String getEntityUserSetOrganisationID() {
		return entityUserSetOrganisationID;
	}
	public String getEntityClassName() {
		return entityClassName;
	}
	public String getEntityUserSetID() {
		return entityUserSetID;
	}

	public String getAuthorizedObjectID() {
		return authorizedObjectID;
	}
	public AuthorizedObjectID getAuthorizedObjectIDAsOID() {
		return (AuthorizedObjectID) ObjectIDUtil.createObjectID(authorizedObjectID);
	}

	public String getEntityObjectIDString() {
		return entityObjectIDString;
	}

//	public ObjectID getEntityObjectIDAsOID() {
//		return entityUserSet.createEntityObjectID(entityObjectIDString);
//	}

	public IEntityUserSet<Entity> getEntityUserSet() {
		return entityUserSet;
	}

	public AuthorizedObjectRef<Entity> getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}

	public abstract Entity getEntity();

	public int getReferenceCount() {
		return referenceCount;
	}

	public int incReferenceCount(int count) {
		if (count < 0)
			return decReferenceCount(-count);

		referenceCount += count;
		return referenceCount;
	}

	public int decReferenceCount(int count) {
		if (count < 0)
			return incReferenceCount(-count);

		referenceCount -= count;

		if (referenceCount < 0)
			throw new IllegalStateException("referenceCount < 0");

		return referenceCount;
	}

	public boolean isDirectlyReferenced() {
		return directlyReferenced;
	}

	public void setDirectlyReferenced(boolean directlyReferenced) {
		if (this.directlyReferenced == directlyReferenced)
			return;

		this.directlyReferenced = directlyReferenced;

		if (directlyReferenced)
			incReferenceCount(1);
		else
			decReferenceCount(1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityUserSetOrganisationID == null) ? 0 : entityUserSetOrganisationID.hashCode());
		result = prime * result + ((entityClassName == null) ? 0 : entityClassName.hashCode());
		result = prime * result + ((entityUserSetID == null) ? 0 : entityUserSetID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		result = prime * result + ((entityObjectIDString == null) ? 0 : entityObjectIDString.hashCode());
		return result;

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final EntityRef<?> other = (EntityRef<?>) obj;

		return (
				Util.equals(this.entityObjectIDString, other.entityObjectIDString) &&
				Util.equals(this.authorizedObjectID, other.authorizedObjectID) &&
				Util.equals(this.entityUserSetID, other.entityUserSetID) &&
				Util.equals(this.entityClassName, other.entityClassName) &&
				Util.equals(this.entityUserSetOrganisationID, other.entityUserSetOrganisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + entityUserSetOrganisationID + ',' + entityClassName + ',' + entityUserSetID + ',' + authorizedObjectID + ',' + entityObjectIDString + ']';
	}
}
