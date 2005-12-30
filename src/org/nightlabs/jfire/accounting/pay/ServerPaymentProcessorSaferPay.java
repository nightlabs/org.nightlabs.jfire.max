/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.jfire.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorSaferPay"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorSaferPay extends ServerPaymentProcessor
{
	public static ServerPaymentProcessorSaferPay getServerPaymentProcessorSaferPay(PersistenceManager pm)
	{
		ServerPaymentProcessorSaferPay serverPaymentProcessorSaferPay;
		try {
			pm.getExtent(ServerPaymentProcessorSaferPay.class);
			serverPaymentProcessorSaferPay = (ServerPaymentProcessorSaferPay) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorSaferPay.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorSaferPay = new ServerPaymentProcessorSaferPay(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorSaferPay.class.getName());
			pm.makePersistent(serverPaymentProcessorSaferPay);
		}

		return serverPaymentProcessorSaferPay;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorSaferPay()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorSaferPay(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		Account treasury = getAccountOutside(payParams, "saferPay");
		return treasury;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException
	{
		// TODO here, we should approve externally via the safer pay system.
		// probably, we would need a subclass of PaymentResult to store a safer pay
		// approval key. But this is only necessary, if we cannot use our paymentID.

		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_APPROVED_WITH_EXTERNAL,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_PAID_WITH_EXTERNAL,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
//	 TODO here, we should commit the previously approved payment.

		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_COMMITTED_WITH_EXTERNAL,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayRollback(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException
	{
//	 TODO here, we should rollback the previously approved payment.

		return new PaymentResult(
				payParams.accounting.getOrganisationID(),
				PaymentResult.CODE_ROLLED_BACK_WITH_EXTERNAL,
				(String)null,
				(Throwable)null);
	}

}
