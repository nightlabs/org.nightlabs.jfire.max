package org.nightlabs.jfire.store;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	private RoleConstants() { }

	public static final RoleID queryDeliveryNotes = RoleID.create("org.nightlabs.jfire.store.queryDeliveryNotes");

	public static final RoleID editDeliveryNote = RoleID.create("org.nightlabs.jfire.store.editDeliveryNote");

	public static final RoleID deliver = RoleID.create("org.nightlabs.jfire.store.deliver");

	public static final RoleID seeProductType = RoleID.create("org.nightlabs.jfire.store.seeProductType");

	public static final RoleID editUnconfirmedProductType = RoleID.create("org.nightlabs.jfire.store.editUnconfirmedProductType");

	public static final RoleID editConfirmedProductType = RoleID.create("org.nightlabs.jfire.store.editConfirmedProductType");
}
