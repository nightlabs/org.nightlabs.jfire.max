package org.nightlabs.jfire.entityuserset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.util.Util;

/**
 * Base-class for assignments of some entities to users or
 * user-groups (more precisely {@link UserLocal}s or {@link UserSecurityGroup}s).
 * <p>
 * See <a href="https://www.jfire.org/modules/phpwiki/index.php/Framework%20EntityUserSet">Framework EntityUserSet</a>
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.id.EntityUserSetID"
 *		detachable="true"
 *		table="JFireEntityUserSet_EntityUserSet"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.create-objectid-class field-order="organisationID, entityClassName, entityUserSetID"
 *
 * @jdo.fetch-group name="IEntityUserSet.authorizedObjectRefs" fields="authorizedObjectRefs"
 * @jdo.fetch-group name="IEntityUserSet.name" fields="name"
 * @jdo.fetch-group name="IEntityUserSet.description" fields="description"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="authorizedObjectRefs, name, description"
 */
public abstract class EntityUserSet<Entity>
implements Serializable, IEntityUserSet<Entity>
{
	private static final long serialVersionUID = 2L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

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
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.entityuserset.AuthorizedObjectRef"
	 *		mapped-by="entityUserSet"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="authorizedObjectID"
	 */
	private Map<String, AuthorizedObjectRef<Entity>> authorizedObjectRefs;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient EntityUserSetController entityUserSetController;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="entityUserSet" dependent="true"
	 */
	private EntityUserSetName name;
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="entityUserSet" dependent="true"
	 */
	private EntityUserSetDescription description;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EntityUserSet() { }

	public EntityUserSet(Class<? extends Entity> entityClass) {
		this(
				IDGenerator.getOrganisationID(),
				entityClass,
				ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(EntityUserSet.class, entityClass.getName()))
		);
	}

	public EntityUserSet(String organisationID, Class<? extends Entity> entityClass, String entityUserSetID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(entityUserSetID, "entityUserSetID");
		this.organisationID = organisationID;
		this.entityClassName = entityClass.getName();
		this.entityUserSetID = entityUserSetID;

		this.name = new EntityUserSetName(this);
		this.description = new EntityUserSetDescription(this);

		authorizedObjectRefs = new HashMap<String, AuthorizedObjectRef<Entity>>();
	}

	@Override
	public void setEntityUserSetController(EntityUserSetController entityUserSetController) {
		this.entityUserSetController = entityUserSetController;
	}

	@Override
	public EntityUserSetController getEntityUserSetController() {
		return entityUserSetController;
	}

	@Override
	public EntityUserSetController getEntityUserSetController(boolean throwExceptionIfNotAssigned) {
		if (entityUserSetController == null)
			throw new IllegalStateException("There is no EntityUserSetController assigned! You must call setEntityUserSetController(...) before!");

		return entityUserSetController;
	}

	@Override
	public String getOrganisationID() {
		return organisationID;
	}
	@Override
	public String getEntityClassName() {
		return entityClassName;
	}
	@Override
	public String getEntityUserSetID() {
		return entityUserSetID;
	}

	@Override
	public EntityUserSetName getName() {
		return name;
	}

	@Override
	public EntityUserSetDescription getDescription() {
		return description;
	}

	protected AuthorizedObjectRef<Entity> createOrGetAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef<Entity> authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null) {
			authorizedObjectRef = createAuthorizedObjectRef(authorizedObjectID);
			authorizedObjectRefs.put(authorizedObjectIDAsString, authorizedObjectRef);
		};
		return authorizedObjectRef;
	}

	protected abstract AuthorizedObjectRef<Entity> createAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID);

	private void addAuthorizedObjectIndirectly(AuthorizedObjectID authorizedObjectID)
	{
		AuthorizedObjectRef<Entity> authorizedObjectRef = createOrGetAuthorizedObjectRef(authorizedObjectID);
		authorizedObjectRef.incReferenceCount();

		Set<AuthorizedObjectID> memberIDs = getEntityUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				addAuthorizedObjectIndirectly(memberID);
			}
		}
	}

	private void removeAuthorizedObjectIndirectly(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef<Entity> authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null)
			return;

		Set<AuthorizedObjectID> memberIDs = getEntityUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				removeAuthorizedObjectIndirectly(memberID);
			}
		}

		if (authorizedObjectRef.decReferenceCount() == 0)
			authorizedObjectRefs.remove(authorizedObjectIDAsString);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#addAuthorizedObject(org.nightlabs.jfire.security.id.AuthorizedObjectID)
	 */
	public AuthorizedObjectRef<Entity> addAuthorizedObject(AuthorizedObjectID authorizedObjectID)
	{
		AuthorizedObjectRef<Entity> authorizedObjectRef = createOrGetAuthorizedObjectRef(authorizedObjectID);
		if (authorizedObjectRef.isDirectlyReferenced())
			return authorizedObjectRef;

		authorizedObjectRef.setDirectlyReferenced(true);

		Set<AuthorizedObjectID> memberIDs = getEntityUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				addAuthorizedObjectIndirectly(memberID);
			}
		}

		return authorizedObjectRef;
	}

	public void retainAuthorizedObjectsForReplication(UserLocalID principalAuthorizedObjectID)
	{
		Collection<AuthorizedObjectRef<Entity>> authorizedObjectRefsToRemove = new LinkedList<AuthorizedObjectRef<Entity>>();
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : authorizedObjectRefs.values()) {
			if (!principalAuthorizedObjectID.equals(authorizedObjectRef.getAuthorizedObjectIDAsOID()))
				authorizedObjectRefsToRemove.add(authorizedObjectRef);
		}
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : authorizedObjectRefsToRemove)
			 authorizedObjectRefs.remove(authorizedObjectRef.getAuthorizedObjectID());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#retainAuthorizedObjects(java.util.Collection)
	 */
	@Override
	public void retainAuthorizedObjects(Collection<? extends AuthorizedObjectID> authorizedObjectIDs)
	{
		Collection<AuthorizedObjectRef<Entity>> authorizedObjectRefsToRemove = new LinkedList<AuthorizedObjectRef<Entity>>();
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : authorizedObjectRefs.values()) {
			if (!authorizedObjectIDs.contains(authorizedObjectRef.getAuthorizedObjectIDAsOID()))
				authorizedObjectRefsToRemove.add(authorizedObjectRef);
		}
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : authorizedObjectRefsToRemove)
			this.removeAuthorizedObject(authorizedObjectRef.getAuthorizedObjectIDAsOID());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#removeAuthorizedObjects(java.util.Collection)
	 */
	public void removeAuthorizedObjects(Collection<? extends AuthorizedObjectID> authorizedObjectIDs)
	{
		for (AuthorizedObjectID authorizedObjectID : authorizedObjectIDs)
			removeAuthorizedObject(authorizedObjectID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#removeAuthorizedObject(org.nightlabs.jfire.security.id.AuthorizedObjectID)
	 */
	public void removeAuthorizedObject(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef<Entity> authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		if (authorizedObjectRef == null)
			return;

		if (!authorizedObjectRef.isDirectlyReferenced())
			return;

		for (EntityRef<Entity> entityRef : new ArrayList<EntityRef<Entity>>(authorizedObjectRef.getEntityRefs())) {
			authorizedObjectRef.removeEntity(entityRef.getEntity());
		}

		authorizedObjectRef.setDirectlyReferenced(false);

		Set<AuthorizedObjectID> memberIDs = getEntityUserSetController(true).getUserSecurityGroupMemberIDs(authorizedObjectID);
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				removeAuthorizedObjectIndirectly(memberID);
			}
		}

		if (authorizedObjectRef.getReferenceCount() == 0)
			authorizedObjectRefs.remove(authorizedObjectIDAsString);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#getAuthorizedObjectRef(org.nightlabs.jfire.security.id.AuthorizedObjectID)
	 */
	public AuthorizedObjectRef<Entity> getAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID)
	{
		String authorizedObjectIDAsString = authorizedObjectID.toString();
		AuthorizedObjectRef<Entity> authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectIDAsString);
		return authorizedObjectRef;
	}

	protected void internalRemoveAuthorizedObjectRef(AuthorizedObjectRef<Entity> authorizedObjectRef) {
		if (!this.equals(authorizedObjectRef.getEntityUserSet()))
			throw new IllegalArgumentException("this != authorizedObjectRef.getEntityUserSet() :: " + this + " != " + authorizedObjectRef.getEntityUserSet());

		AuthorizedObjectRef<Entity> internalAuthorizedObjectRef = authorizedObjectRefs.remove(authorizedObjectRef.getAuthorizedObjectID());
		if (internalAuthorizedObjectRef == null)
			throw new IllegalStateException("AuthorizedObjectRef not found! " + authorizedObjectRef);

		if (!internalAuthorizedObjectRef.equals(authorizedObjectRef))
			throw new IllegalStateException("internalAuthorizedObjectRef != authorizedObjectRef :: " + internalAuthorizedObjectRef + " != " + authorizedObjectRef);

		if (internalAuthorizedObjectRef.getReferenceCount() != 0)
			throw new IllegalStateException("internalAuthorizedObjectRef.referenceCount != 0 :: internalAuthorizedObjectRef = " + internalAuthorizedObjectRef + " :: internalAuthorizedObjectRef.referenceCount = " + internalAuthorizedObjectRef.getReferenceCount());

	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.IEntityUserSet#getAuthorizedObjectRefs()
	 */
	public Collection<AuthorizedObjectRef<Entity>> getAuthorizedObjectRefs() {
		return Collections.unmodifiableCollection(authorizedObjectRefs.values());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((entityUserSetID == null) ? 0 : entityUserSetID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final EntityUserSet<?> other = (EntityUserSet<?>) obj;

		return (
				Util.equals(this.entityUserSetID, other.entityUserSetID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + entityUserSetID + ']';
	}

	protected abstract EntityRef<Entity> createEntityRef(AuthorizedObjectRef<Entity> authorizedObjectRef, Entity entity);

	protected String getEntityObjectIDString(Entity entity)
	{
		if (entity == null)
			throw new IllegalArgumentException("entity must not be null!");

		ObjectID entityObjectID = (ObjectID)JDOHelper.getObjectId(entity);

		if (entityObjectID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(entity) returned null! entity=" + entity);

		String entityObjectIDString = entityObjectID.toString();
		return entityObjectIDString;
	}

//	protected ObjectID createEntityObjectID(String entityObjectID) {
//		return ObjectIDUtil.createObjectID(entityObjectID);
//	}
}
