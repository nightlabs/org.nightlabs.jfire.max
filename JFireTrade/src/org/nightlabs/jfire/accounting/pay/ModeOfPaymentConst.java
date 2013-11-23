package org.nightlabs.jfire.accounting.pay;

import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.organisation.Organisation;

public class ModeOfPaymentConst
{
	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_CASH = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "cash");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_CASH = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "cash");

	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_NON_PAYMENT = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "nonPayment");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_NON_PAYMENT = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "nonPayment");

	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_CREDIT_CARD = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "creditCard");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_VISA = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "visa");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_MASTER_CARD = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "masterCard");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_AMERICAN_EXPRESS = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "americanExpress");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_DINERS_CLUB = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "dinersClub");

	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_BANK_TRANSFER = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "bankTransfer");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_BANK_TRANSFER = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "bankTransfer");

	public static final ModeOfPaymentID MODE_OF_PAYMENT_ID_DEBIT_NOTE = ModeOfPaymentID.create(Organisation.DEV_ORGANISATION_ID, "debitNote");
	public static final ModeOfPaymentFlavourID MODE_OF_PAYMENT_FLAVOUR_ID_DEBIT_NOTE = ModeOfPaymentFlavourID.create(Organisation.DEV_ORGANISATION_ID, "debitNote");
}
