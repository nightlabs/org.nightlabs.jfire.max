package org.nightlabs.jfire.voucher.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
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
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerPaymentProcessorVoucher
		extends ServerPaymentProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerPaymentProcessorVoucher getServerPaymentProcessorVoucher(PersistenceManager pm)
	{
		ServerPaymentProcessorVoucher serverPaymentProcessorVoucher;
		try {
			pm.getExtent(ServerPaymentProcessorVoucher.class);
			serverPaymentProcessorVoucher = (ServerPaymentProcessorVoucher) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorVoucher.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorVoucher = new ServerPaymentProcessorVoucher(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorVoucher.class.getName());
			serverPaymentProcessorVoucher = (ServerPaymentProcessorVoucher) pm.makePersistent(serverPaymentProcessorVoucher);
		}

		return serverPaymentProcessorVoucher;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorVoucher() { }

	public ServerPaymentProcessorVoucher(String organisationID, String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	@Implement
	protected PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	protected PaymentResult externalPayCommit(PayParams payParams)
			throws PaymentException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	protected PaymentResult externalPayRollback(PayParams payParams)
			throws PaymentException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	public Anchor getAnchorOutside(PayParams payParams)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
