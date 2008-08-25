package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	private RoleConstants() {}

	public static final RoleID sellProductType = RoleID.create("org.nightlabs.jfire.trade.sellProductType");
	public static final RoleID reverseProductType = RoleID.create("org.nightlabs.jfire.trade.reverseProductType");

	public static final RoleID createOrder = RoleID.create("org.nightlabs.jfire.trade.createOrder");
	public static final RoleID createOffer = RoleID.create("org.nightlabs.jfire.trade.createOffer");
	public static final RoleID queryOffers = RoleID.create("org.nightlabs.jfire.trade.queryOffers");
	public static final RoleID queryOrders = RoleID.create("org.nightlabs.jfire.trade.queryOrders");
}
