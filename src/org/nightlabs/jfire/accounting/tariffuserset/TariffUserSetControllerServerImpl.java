package org.nightlabs.jfire.accounting.tariffuserset;

import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;

public class TariffUserSetControllerServerImpl
extends TariffUserSetController
{
	private PersistenceManager pm;

	public TariffUserSetControllerServerImpl(PersistenceManager pm) {
		this.pm = pm;
	}

	@Override
	public Set<AuthorizedObjectID> getUserSecurityGroupMemberIDs(AuthorizedObjectID authorizedObjectID)
	{
		Object o = pm.getObjectById(authorizedObjectID);
		if (!(o instanceof UserSecurityGroup))
			return null;

		UserSecurityGroup userSecurityGroup = (UserSecurityGroup) o;
		Set<AuthorizedObject> members = userSecurityGroup.getMembers();
		return NLJDOHelper.getObjectIDSet(members);
	}
}
