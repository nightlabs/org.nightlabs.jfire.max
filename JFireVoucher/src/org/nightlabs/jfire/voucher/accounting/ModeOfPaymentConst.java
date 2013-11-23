package org.nightlabs.jfire.voucher.accounting;

import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.organisation.Organisation;

public class ModeOfPaymentConst
{
	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_VOUCHER = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "voucher");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_VOUCHER = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "voucher");
}
