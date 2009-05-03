package org.nightlabs.jfire.voucher;

import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class JFireVoucherEAR
{
	public static final String MODULE_NAME = JFireVoucherEAR.class.getSimpleName();
	public static final String DEFAULT_DELIVERY_CONFIGURATION_ID = "JFireVoucher.default"; //$NON-NLS-1$
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_VOUCHER_PRINT =
		ModeOfDeliveryID.create(Organisation.DEV_ORGANISATION_ID, "voucherPrint"); //$NON-NLS-1$
	public static final ModeOfDeliveryFlavourID MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_OPERATING_SYSTEM_PRINTER =
		ModeOfDeliveryFlavourID.create(Organisation.DEV_ORGANISATION_ID, "voucherPrintViaOperatingSystemPrinter"); //$NON-NLS-1$
	public static final ModeOfDeliveryFlavourID MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_TICKET_PRINTER =
		ModeOfDeliveryFlavourID.create(Organisation.DEV_ORGANISATION_ID, "voucherPrintViaTicketPrinter"); //$NON-NLS-1$
	public static final AccountTypeID ACCOUNT_TYPE_ID_VOUCHER = AccountTypeID.create(Organisation.DEV_ORGANISATION_ID, "Account.Voucher"); //$NON-NLS-1$
}
