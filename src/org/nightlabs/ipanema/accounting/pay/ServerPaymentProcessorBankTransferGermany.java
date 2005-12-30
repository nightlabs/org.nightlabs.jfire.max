/*
 * Created on Jun 18, 2005
 */
package org.nightlabs.ipanema.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.ipanema.organisation.Organisation;
import org.nightlabs.ipanema.transfer.Anchor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorBankTransferGermany"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorBankTransferGermany extends ServerPaymentProcessor
{
	public static ServerPaymentProcessorBankTransferGermany getServerPaymentProcessorBankTransferGermany(PersistenceManager pm)
	{
		ServerPaymentProcessorBankTransferGermany serverPaymentProcessorBankTransfer;
		try {
			pm.getExtent(ServerPaymentProcessorBankTransferGermany.class);
			serverPaymentProcessorBankTransfer = (ServerPaymentProcessorBankTransferGermany) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorBankTransferGermany.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorBankTransfer = new ServerPaymentProcessorBankTransferGermany(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorBankTransferGermany.class.getName());
//			pm.makePersistent(serverPaymentProcessorBankTransfer);
		}

		return serverPaymentProcessorBankTransfer;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorBankTransferGermany()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorBankTransferGermany(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		return getAccountOutside(payParams, "bankTransfer");
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException
	{
		PaymentDataBankTransferGermany paymentDataBankTransfer = null;
		if (payParams.paymentData instanceof PaymentDataBankTransferGermany)
			paymentDataBankTransfer = (PaymentDataBankTransferGermany) payParams.paymentData;

		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					payParams.accounting.getOrganisationID(),
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the customer has
		// transferred the money.
		// TODO there still has to be done a lot - maybe completely different (e.g. with
		// two different processors)
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					payParams.accounting.getOrganisationID(),
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the customer has
		// transferred the money.
//	 TODO there still has to be done a lot - maybe completely different (e.g. with
		// two different processors)
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					payParams.accounting.getOrganisationID(),
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the customer has
		// transferred the money.
//	 TODO there still has to be done a lot - maybe completely different (e.g. with
		// two different processors)
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
