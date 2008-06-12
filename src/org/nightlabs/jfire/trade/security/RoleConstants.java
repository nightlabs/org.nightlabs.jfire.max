package org.nightlabs.jfire.trade.security;

import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.store.StoreManager;

public class RoleConstants {
	private RoleConstants() {}

	public static final RoleID storeManager_getProductType = RoleID.create(StoreManager.class.getName() + "#getProductType");

//	public static final RoleGroupID seeProductType = RoleGroupID.create("org.nightlabs.jfire.trade.seeProductType");
//
//	public static final RoleGroupID sellProductType = RoleGroupID.create("org.nightlabs.jfire.trade.sellProductType");
//	public static final RoleGroupID reverseProductType = RoleGroupID.create("org.nightlabs.jfire.trade.reverseProductType");
//
//	public static final RoleGroupID editProductType = RoleGroupID.create("org.nightlabs.jfire.trade.editProductType");
}
