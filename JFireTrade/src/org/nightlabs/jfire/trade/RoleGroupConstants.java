package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.security.id.RoleGroupID;

public class RoleGroupConstants {
	private RoleGroupConstants() { }

	public static final RoleGroupID sellProductType = RoleGroupID.create(RoleConstants.sellProductType.roleID);

	public static final RoleGroupID reverseProductType = RoleGroupID.create(RoleConstants.reverseProductType.roleID);
}
