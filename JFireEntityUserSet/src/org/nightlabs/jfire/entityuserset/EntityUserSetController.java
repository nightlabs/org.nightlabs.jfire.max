package org.nightlabs.jfire.entityuserset;

import java.util.Set;

import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;

public abstract class EntityUserSetController
{
	/**
	 * Get the object-ids of all members of the specified {@link UserSecurityGroup}. If
	 * the specified {@code authorizedObjectID} does not reference an instance of <code>UserSecurityGroup</code>,
	 * this method returns <code>null</code>.
	 *
	 * @param authorizedObjectID
	 * @return <code>null</code> or the object-ids of the members of the specified {@link UserSecurityGroup}.
	 */
	public abstract Set<AuthorizedObjectID> getUserSecurityGroupMemberIDs(AuthorizedObjectID authorizedObjectID);
}
