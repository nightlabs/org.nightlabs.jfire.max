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
 *		table="JFireTrade_ServerPaymentProcessorDebitNoteGermany"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorDebitNoteGermany extends ServerPaymentProcessor
{
	public static ServerPaymentProcessorDebitNoteGermany getServerPaymentProcessorDebitNoteGermany(PersistenceManager pm)
	{
		ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNote;
		try {
			pm.getExtent(ServerPaymentProcessorDebitNoteGermany.class);
			serverPaymentProcessorDebitNote = (ServerPaymentProcessorDebitNoteGermany) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorDebitNoteGermany.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorDebitNote = new ServerPaymentProcessorDebitNoteGermany(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorDebitNoteGermany.class.getName());
			pm.makePersistent(serverPaymentProcessorDebitNote);
		}

		return serverPaymentProcessorDebitNote;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorDebitNoteGermany()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorDebitNoteGermany(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		return getAccountOutside(payParams, "debitNote");
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					payParams.accounting.getOrganisationID(),
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
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

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					payParams.accounting.getOrganisationID(),
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.pay.ServerPaymentProcessor#externalPayRollback(PayParams)
	 */
	protected PaymentResult externalPayRollback(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

}
