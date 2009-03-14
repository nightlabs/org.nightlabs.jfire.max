package org.nightlabs.jfire.entityuserset;

import java.util.Collection;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;

public interface IEntityUserSet<Entity>
{
	static final String FETCH_GROUP_AUTHORIZED_OBJECT_REFS = "IEntityUserSet.authorizedObjectRefs";
	static final String FETCH_GROUP_NAME = "IEntityUserSet.name";
	static final String FETCH_GROUP_DESCRIPTION = "IEntityUserSet.description";

	void setEntityUserSetController(
			EntityUserSetController entityUserSetController);

	EntityUserSetController getEntityUserSetController();

	EntityUserSetController getEntityUserSetController(boolean throwExceptionIfNotAssigned);

	String getOrganisationID();

	String getEntityUserSetID();

	EntityUserSetName getName();

	EntityUserSetDescription getDescription();

	/**
	 * Add a {@link User} (via the object-id of its {@link UserLocal}) or a {@link UserSecurityGroup}
	 * (via its {@link UserSecurityGroupID}) to this <code>EntityUserSet</code>.
	 *
	 * @param authorizedObjectID
	 */
	AuthorizedObjectRef<Entity> addAuthorizedObject(AuthorizedObjectID authorizedObjectID);

	void retainAuthorizedObjects(Collection<? extends AuthorizedObjectID> authorizedObjectIDs);

	void removeAuthorizedObjects(Collection<? extends AuthorizedObjectID> authorizedObjectIDs);

	void removeAuthorizedObject(AuthorizedObjectID authorizedObjectID);

	AuthorizedObjectRef<Entity> getAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID);

	Collection<AuthorizedObjectRef<Entity>> getAuthorizedObjectRefs();
}