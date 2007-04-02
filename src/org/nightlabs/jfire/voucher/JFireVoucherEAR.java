package org.nightlabs.jfire.voucher;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class JFireVoucherEAR 
{
	public static final String MODULE_NAME = "JFireVoucher";
	public static final String DEFAULT_DELIVERY_CONFIGURATION_ID = "JFireVoucher.default";
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_VOUCHER_PRINT = 
		ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "voucherPrint");
	public static final ModeOfDeliveryFlavourID MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_OPERATING_SYSTEM_PRINTER = 
		ModeOfDeliveryFlavourID.create(Organisation.DEVIL_ORGANISATION_ID, "voucherPrintViaOperatingSystemPrinter");
}
