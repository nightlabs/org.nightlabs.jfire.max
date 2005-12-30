/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.ipanema.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.accounting.Account;
import org.nightlabs.ipanema.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.ipanema.organisation.Organisation;
import org.nightlabs.ipanema.transfer.Anchor;

/**
 * This module does nothing and is used for client-side handled credit card payments.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorCreditCardDummyForClientPayment"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorCreditCardDummyForClientPayment extends ServerPaymentProcessor
{
	public static ServerPaymentProcessorCreditCardDummyForClientPayment getServerPaymentProcessorCreditCardDummyForClientPayment(PersistenceManager pm)
	{
		ServerPaymentProcessorCreditCardDummyForClientPayment serverPaymentProcessorSaferPay;
		try {
			pm.getExtent(ServerPaymentProcessorCreditCardDummyForClientPayment.class);
			serverPaymentProcessorSaferPay = (ServerPaymentProcessorCreditCardDummyForClientPayment) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorCreditCardDummyForClientPayment.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorSaferPay = new ServerPaymentProcessorCreditCardDummyForClientPayment(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorCreditCardDummyForClientPayment.class.getName());
			pm.makePersistent(serverPaymentProcessorSaferPay);
		}

		return serverPaymentProcessorSaferPay;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorCreditCardDummyForClientPayment()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorCreditCardDummyForClientPayment(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		Account treasury = getAccountOutside(payParams, "clientSidedCreditCardPayment");
		return treasury;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException
	{
//		return new PaymentResult(
//				payParams.accounting.getOrganisationID(),
//				PaymentResult.CODE_POSTPONED,
//				null, null);
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayRollback(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException
	{
		return null;
	}

}
