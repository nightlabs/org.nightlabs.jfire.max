/*
 * Created 	on Feb 17, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.pay;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.TransferRegistry;
import org.nightlabs.jfire.transfer.id.TransferID;

/**
 * PayMoneyTransfers are generated on payments. One anchor of PayMoneyTransfer is
 * always a "treasury", which is a special
 * {@link org.nightlabs.jfire.transfer.Anchor} (usually an
 * {@link org.nightlabs.jfire.accounting.Account}) representing
 * a money source OUTSIDE the system. The other <tt>Anchor</tt> is a
 * {@link org.nightlabs.jfire.trade.LegalEntity} representing the partner to whom
 * or from whom money is transferred.
 * <p>
 * In spite of other {@link org.nightlabs.jfire.accounting.MoneyTransfer}s
 * that are used for bookings and are assigned to one invoice only
 * the invoice-member (inherited from MoneyTransfer) of PayMoneyTransfer will be null.
 * A <tt>PayMoneyTransfer</tt> 
 * can be assigned to a multiple of {@link org.nightlabs.jfire.accounting.Invoice}
 * s.
 * <p>
 * Additionally, every <tt>MoneyTransfer</tt> can occur without being linked to any
 * <tt>Invoice</tt> (e.g. if the partner decides later to do a withdrawal of some
 * "old" money kept after reimbourse).
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_PayMoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 *		name = "getPayMoneyTransferForPayment"
 *		query = "SELECT UNIQUE
 *				WHERE
 *					this.payment.organisationID == paramOrganisationID &&
 *					this.payment.paymentID == paramPaymentID
 *				PARAMETERS String paramOrganisationID, String paramPaymentID
 *				IMPORTS import java.lang.String"
 */
public class PayMoneyTransfer extends MoneyTransfer
{
	public static Collection getChildren(
			PersistenceManager pm, TransferID transferID)
	{
		return getChildren(pm, transferID.organisationID, transferID.transferTypeID, transferID.transferID);
	}

	public static Collection getChildren(
			PersistenceManager pm, String organisationID, String transferTypeID, long transferID)
	{
		Query query = pm.newQuery(MoneyTransfer.class);
		query.declareImports("import java.lang.String");
		query.declareParameters("String paramOrganisationID, String paramTransferTypeID, long paramTransferID");
		query.setFilter(
				"this.container.organisationID == paramOrganisationID && " +
				"this.container.transferTypeID == paramTransferTypeID && " +
				"this.container.transferID == paramTransferID");
		return (Collection)query.execute(organisationID, transferTypeID, new Long(transferID));
	}

	/**
	 * This method searches via JDO all <tt>MoneyTransfer</tt>s which have
	 * {@link Transfer#getContainer()}<tt> == this</tt>.
	 */
	public Collection getChildren()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Currently not attached to datastore! Cannot obtain my PersistenceManager!");
		return getChildren(pm, getOrganisationID(), getTransferTypeID(), getTransferID());
	}

	public static PayMoneyTransfer getPayMoneyTransferForPayment(
			PersistenceManager pm,
			PaymentID paymentID)
	{
		return getPayMoneyTransferForPayment(pm, paymentID.organisationID, paymentID.paymentID);
	}

	public static PayMoneyTransfer getPayMoneyTransferForPayment(
			PersistenceManager pm,
			Payment payment)
	{
		return getPayMoneyTransferForPayment(pm, payment.getOrganisationID(), payment.getPaymentID());
	}

	/**
	 * @param pm The PM to access the datastore.
	 * @param organisationID see {@link Payment#getOrganisationID()}
	 * @param paymentID see {@link Payment#getPaymentID()}
	 *
	 * @return Either <tt>null</tt> or the instance of <tt>PayMoneyTransfer</tt> that
	 *		has previously been created for the specified {@link Payment}.
	 */
	public static PayMoneyTransfer getPayMoneyTransferForPayment(
			PersistenceManager pm,
			String organisationID, String paymentID)
	{
		Query query = pm.newNamedQuery(PayMoneyTransfer.class, "getPayMoneyTransferForPayment");
		return (PayMoneyTransfer) query.execute(organisationID, paymentID);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Payment payment;

	/**
	 * @deprecated Only for JDO!
	 */
	protected PayMoneyTransfer() { }

	/**
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param invoice
	 * @param currency
	 * @param amount
	 */
	public PayMoneyTransfer(TransferRegistry transferRegistry,
			Transfer container, User initiator, Anchor from, Anchor to,
			Payment payment)
	{		
		super(transferRegistry, container, initiator, from, to, payment.getCurrency(),
				payment.getAmount());
		this.payment = payment;
	}

	/**
	 * @return Returns the payment.
	 */
	public Payment getPayment()
	{
		return payment;
	}
}
