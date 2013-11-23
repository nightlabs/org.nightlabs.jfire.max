package org.nightlabs.jfire.store;

import org.nightlabs.jfire.security.id.RoleGroupID;

public class RoleGroupConstants {
	private RoleGroupConstants() { }

	public static final RoleGroupID seeProductType = RoleGroupID.create(RoleConstants.seeProductType.roleID);
}
