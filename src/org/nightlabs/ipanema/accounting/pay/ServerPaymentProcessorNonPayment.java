/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.ipanema.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.accounting.Account;
import org.nightlabs.ipanema.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.ipanema.organisation.Organisation;
import org.nightlabs.ipanema.transfer.Anchor;


/**
 * This payment processor handles non-payments. This means, it doesn't
 * do anything but cause the payment to be postponed.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorNonPayment"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorNonPayment
extends ServerPaymentProcessor
{
	public static ServerPaymentProcessorNonPayment getServerPaymentProcessorNonPayment(PersistenceManager pm)
	{
		ServerPaymentProcessorNonPayment serverPaymentProcessorNonPayment;
		try {
			pm.getExtent(ServerPaymentProcessorNonPayment.class);
			serverPaymentProcessorNonPayment = (ServerPaymentProcessorNonPayment) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorNonPayment.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorNonPayment = new ServerPaymentProcessorNonPayment(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorNonPayment.class.getName());
			pm.makePersistent(serverPaymentProcessorNonPayment);
		}

		return serverPaymentProcessorNonPayment;
	}

	/**
	 * @deprecated Only for JDO! 
	 */
	protected ServerPaymentProcessorNonPayment()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorNonPayment(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		Account treasury = getAccountOutside(payParams, "nonPayment");
		return treasury;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException
	{
		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayRollback(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException
	{
		return null;
	}

}
