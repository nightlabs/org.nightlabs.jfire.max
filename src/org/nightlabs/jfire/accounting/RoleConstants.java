package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	private RoleConstants() { }

	public static final RoleID editTariffMapping = RoleID.create("org.nightlabs.jfire.accounting.editTariffMapping");

	public static final RoleID editTariff = RoleID.create("org.nightlabs.jfire.accounting.editTariff");

	public static final RoleID queryAccounts = RoleID.create("org.nightlabs.jfire.accounting.queryAccounts");

	public static final RoleID editAccount = RoleID.create("org.nightlabs.jfire.accounting.editAccount");

	public static final RoleID queryLocalAccountantDelegates = RoleID.create("org.nightlabs.jfire.accounting.queryLocalAccountantDelegates");

	public static final RoleID editLocalAccountantDelegate = RoleID.create("org.nightlabs.jfire.accounting.editLocalAccountantDelegate");

	public static final RoleID queryInvoices = RoleID.create("org.nightlabs.jfire.accounting.queryInvoices");

	public static final RoleID editInvoice = RoleID.create("org.nightlabs.jfire.accounting.editInvoice");

	public static final RoleID pay = RoleID.create("org.nightlabs.jfire.accounting.pay");

	public static final RoleID editPriceConfiguration = RoleID.create("org.nightlabs.jfire.accounting.editPriceConfiguration");

	public static final RoleID manualMoneyTransfer = RoleID.create("org.nightlabs.jfire.accounting.manualMoneyTransfer");

	public static final RoleID queryMoneyTransfers = RoleID.create("org.nightlabs.jfire.accounting.queryMoneyTransfers");

	public static final RoleID editPriceFragmentType = RoleID.create("org.nightlabs.jfire.accounting.editPriceFragmentType");
}
