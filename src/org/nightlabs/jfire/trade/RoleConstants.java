package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	private RoleConstants() {}

	public static final RoleID sellProductType = RoleID.create("org.nightlabs.jfire.trade.sellProductType");

	public static final RoleID reverseProductType = RoleID.create("org.nightlabs.jfire.trade.reverseProductType");

	public static final RoleID editOrder = RoleID.create("org.nightlabs.jfire.trade.editOrder");

	public static final RoleID editOffer = RoleID.create("org.nightlabs.jfire.trade.editOffer");

	public static final RoleID queryOffers = RoleID.create("org.nightlabs.jfire.trade.queryOffers");

	public static final RoleID queryOrders = RoleID.create("org.nightlabs.jfire.trade.queryOrders");

	public static final RoleID editCustomerGroupMapping = RoleID.create("org.nightlabs.jfire.trade.editCustomerGroupMapping");

	public static final RoleID editCustomerGroup = RoleID.create("org.nightlabs.jfire.trade.editCustomerGroup");
}
