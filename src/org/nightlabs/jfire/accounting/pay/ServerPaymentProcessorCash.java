/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.jfire.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;


/**
 * This payment processor handles cash payments. This basically means, it doesn't
 * do anything as cash is flowing outside the computer and the payment is booked
 * in the accounting by the framework.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorCash"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerPaymentProcessorCash
extends ServerPaymentProcessor
// implements StoreCallback
{
	public static ServerPaymentProcessorCash getServerPaymentProcessorCash(PersistenceManager pm)
	{
		ServerPaymentProcessorCash serverPaymentProcessorCash;
		try {
			pm.getExtent(ServerPaymentProcessorCash.class);
			serverPaymentProcessorCash = (ServerPaymentProcessorCash) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorCash.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorCash = new ServerPaymentProcessorCash(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorCash.class.getName());
			pm.makePersistent(serverPaymentProcessorCash);
		}

		return serverPaymentProcessorCash;
	}

	/**
	 * @deprecated Only for JDO! 
	 */
	protected ServerPaymentProcessorCash()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorCash(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	public Anchor getAnchorOutside(PayParams payParams)
	{
		Account treasury = getAccountOutside(payParams, "cash");
		return treasury;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayRollback(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException
	{
		return null;
	}

//	/**
//	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
//	 */
//	public void jdoPreStore()
//	{
//		// If this processor is persisted the first time, we subscribe the 
//		// ModeOfPayment s
//		if (JDOHelper.isNew(this)) {
//			PersistenceManager pm = getPersistenceManager();
//
//			
//		} // if (JDOHelper.isNew(this)) {
//	}

}
