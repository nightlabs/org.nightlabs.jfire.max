package org.nightlabs.jfire.entityuserset;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.id.AuthorizedObjectRefID"
 *		detachable="true"
 *		table="JFireEntityUserSet_AuthorizedObjectRef"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="entityUserSetOrganisationID, entityUserSetID, authorizedObjectID"
 *
 * @jdo.query
 *		name="getAuthorizedObjectRefsForAuthorizedObjectID"
 *		query="SELECT WHERE this.authorizedObjectID == :authorizedObjectID"
 *
 * @jdo.fetch-group name="AuthorizedObjectRef.entityRefs" fields="entityRefs"
 *
 * @jdo.fetch-group name="EntityUserSet.authorizedObjectRefs" fields="entityUserSet"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="entityUserSet, entityRefs"
 */
public class AuthorizedObjectRef<Entity>
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ENTITY_REFS = "AuthorizedObjectRef.entityRefs";

	public static Collection<? extends AuthorizedObjectRef<?>> getAuthorizedObjectRefs(PersistenceManager pm, AuthorizedObjectID authorizedObjectID)
	{
		Query q = pm.newNamedQuery(AuthorizedObjectRef.class, "getAuthorizedObjectRefsForAuthorizedObjectID");
		return CollectionUtil.castCollection((Collection<?>) q.execute(authorizedObjectID.toString()));
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String entityUserSetOrganisationID;
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private EntityUserSet<Entity> entityUserSet;

	/**
	 * The number of both direct and indirect references to this <code>AuthorizedObjectRef</code>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int referenceCount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean directlyReferenced;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="EntityRef"
	 *		dependent-value="true"
	 *		mapped-by="authorizedObjectRef"
	 *
	 * @jdo.key mapped-by="entityObjectIDString"
	 */
	private Map<String, EntityRef<Entity>> entityRefs;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AuthorizedObjectRef() { }

	public AuthorizedObjectRef(EntityUserSet<Entity> entityUserSet, AuthorizedObjectID authorizedObjectID)
	{
		this.entityUserSet = entityUserSet;
		this.entityUserSetOrganisationID = entityUserSet.getOrganisationID();
		this.entityUserSetID = entityUserSet.getEntityUserSetID();
		this.authorizedObjectID = authorizedObjectID.toString();
		entityRefs = new HashMap<String, EntityRef<Entity>>();
	}

	public String getEntityUserSetOrganisationID() {
		return entityUserSetOrganisationID;
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

	public EntityUserSet<Entity> getEntityUserSet() {
		return entityUserSet;
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public int incReferenceCount() {
		return ++referenceCount;
	}

	public int decReferenceCount() {
		if (--referenceCount < 0)
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
			incReferenceCount();
		else
			decReferenceCount();
	}

	protected EntityRef<Entity> createEntityRef(Entity entity)
	{
		String entityObjectIDString = entityUserSet.getEntityObjectIDString(entity);
		EntityRef<Entity> entityRef = entityRefs.get(entityObjectIDString);
		if (entityRef == null) {
			entityRef = entityUserSet.createEntityRef(this, entity);
			entityRefs.put(entityObjectIDString, entityRef);
		}
		return entityRef;
	}

	public EntityRef<Entity> getEntityRef(Entity entity)
	{
		String entityObjectIDString = entityUserSet.getEntityObjectIDString(entity);
		EntityRef<Entity> entityRef = entityRefs.get(entityObjectIDString);
		return entityRef;
	}

	private void addEntityIndirectly(Entity entity)
	{
		EntityRef<Entity> entityRef = createEntityRef(entity);
		entityRef.incReferenceCount(1);

		Set<AuthorizedObjectID> memberIDs = entityUserSet.getEntityUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef<Entity> memberAuthorizedObjectRef = entityUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("entityUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.addEntityIndirectly(entity);
			}
		}
	}

	private void removeEntityIndirectly(Entity entity)
	{
		String entityObjectIDString = entityUserSet.getEntityObjectIDString(entity);
		EntityRef<Entity> entityRef = entityRefs.get(entityObjectIDString);
		if (entityRef == null)
			return;

		entityRef.decReferenceCount(1);

		Set<AuthorizedObjectID> memberIDs = entityUserSet.getEntityUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef<Entity> memberAuthorizedObjectRef = entityUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("entityUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.removeEntityIndirectly(entity);
			}
		}

		if (entityRef.getReferenceCount() == 0)
			entityRefs.remove(entityObjectIDString);
	}

	public boolean addEntity(Entity entity)
	{
		EntityRef<Entity> entityRef = createEntityRef(entity);
		if (entityRef.isDirectlyReferenced())
			return false; // indicate NO modification

		entityRef.setDirectlyReferenced(true);

		Set<AuthorizedObjectID> memberIDs = entityUserSet.getEntityUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef<Entity> memberAuthorizedObjectRef = entityUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("entityUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.addEntityIndirectly(entity);
			}
		}

		makeEntityUserSetDirty();

		return true; // indicate modification
	}

	private void makeEntityUserSetDirty() {
		JDOHelper.makeDirty(entityUserSet, "authorizedObjectRefs");
	}

	public boolean addEntities(Collection<? extends Entity> entities)
	{
		boolean atLeastOneAdded = false;
		for (Entity entity : entities)
			atLeastOneAdded |= addEntity(entity);

		return atLeastOneAdded;
	}

	public void retainEntities(Collection<? extends Entity> entities)
	{
		Collection<Entity> entitiesToRemove = new LinkedList<Entity>();
		for (EntityRef<Entity> entityRef : this.getEntityRefs()) {
			if (!entities.contains(entityRef.getEntity()))
				entitiesToRemove.add(entityRef.getEntity());
		}

		for (Entity entity : entitiesToRemove)
			this.removeEntity(entity);
	}

	public void removeEntities(Collection<? extends Entity> entities)
	{
		for (Entity entity : entities)
			removeEntity(entity);
	}

	public void removeEntity(Entity entity)
	{
		String entityObjectIDString = entityUserSet.getEntityObjectIDString(entity);
		EntityRef<Entity> entityRef = entityRefs.get(entityObjectIDString);
		if (entityRef == null)
			return;

		if (!entityRef.isDirectlyReferenced())
			return;

		entityRef.setDirectlyReferenced(false);

		Set<AuthorizedObjectID> memberIDs = entityUserSet.getEntityUserSetController(true).getUserSecurityGroupMemberIDs(this.getAuthorizedObjectIDAsOID());
		if (memberIDs != null) {
			for (AuthorizedObjectID memberID : memberIDs) {
				AuthorizedObjectRef<Entity> memberAuthorizedObjectRef = entityUserSet.getAuthorizedObjectRef(memberID);
				if (memberAuthorizedObjectRef == null)
					throw new IllegalStateException("entityUserSet.getAuthorizedObjectRef(memberID) returned null for memberID = " + memberID);

				memberAuthorizedObjectRef.removeEntityIndirectly(entity);
			}
		}

		if (entityRef.getReferenceCount() == 0)
			entityRefs.remove(entityObjectIDString);

		makeEntityUserSetDirty();
	}

	public Collection<EntityRef<Entity>> getEntityRefs() {
		return Collections.unmodifiableCollection(entityRefs.values());
	}

	protected void internalRemoveEntityRef(EntityRef<Entity> entityRef) {
		if (!this.equals(entityRef.getAuthorizedObjectRef()))
			throw new IllegalArgumentException("this != entityRef.getAuthorizedObjectRef() :: " + this + " != " + entityRef.getAuthorizedObjectRef());

		String entityObjectIDString = entityUserSet.getEntityObjectIDString(entityRef.getEntity());

		EntityRef<Entity> internalEntityRef = entityRefs.remove(entityObjectIDString);
		if (internalEntityRef == null)
			throw new IllegalStateException("EntityRef not found! " + entityRef);

		if (!internalEntityRef.equals(entityRef))
			throw new IllegalStateException("internalEntityRef != entityRef :: " + internalEntityRef + " != " + entityRef);

		if (internalEntityRef.getReferenceCount() != 0)
			throw new IllegalStateException("internalEntityRef.referenceCount != 0 :: internalEntityRef = " + internalEntityRef + " :: internalEntityRef.referenceCount = " + internalEntityRef.getReferenceCount());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityUserSetOrganisationID == null) ? 0 : entityUserSetOrganisationID.hashCode());
		result = prime * result + ((entityUserSetID == null) ? 0 : entityUserSetID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final AuthorizedObjectRef<?> other = (AuthorizedObjectRef<?>) obj;

		return (
				Util.equals(this.authorizedObjectID, other.authorizedObjectID) &&
				Util.equals(this.entityUserSetID, other.entityUserSetID) &&
				Util.equals(this.entityUserSetOrganisationID, other.entityUserSetOrganisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + entityUserSetOrganisationID + ',' + entityUserSetID + ',' + authorizedObjectID + ']';
	}
}
