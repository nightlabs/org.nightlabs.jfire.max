/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting.pay;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.TransferID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * PayMoneyTransfers are generated on payments. One anchor of PayMoneyTransfer is
 * always a "treasury", which is a special
 * {@link org.nightlabs.jfire.transfer.Anchor} (usually an
 * {@link org.nightlabs.jfire.accounting.Account}) representing
 * a money source OUTSIDE the system. The other <tt>Anchor</tt> is a
 * {@link org.nightlabs.jfire.trade.LegalEntity} representing the partner to whom
 * or from whom money is transferred.
 * <p>
 * A <tt>PayMoneyTransfer</tt> can be assigned to multiple {@link org.nightlabs.jfire.accounting.Invoice}s
 * by its connecitons to a {@link Payment}.
 * <p>
 * <p>
 * A {@link PayMoneyTransfer} is the container transfer for all sub-transfers made for the
 * associated {@link Payment}.
 * </p>
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
 *				PARAMETERS String paramOrganisationID, Long paramPaymentID
 *				import java.lang.String; import java.lang.Long"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PayMoneyTransfer")
@Queries(
	@javax.jdo.annotations.Query(
		name="getPayMoneyTransferForPayment",
		value="SELECT UNIQUE WHERE this.payment.organisationID == paramOrganisationID && this.payment.paymentID == paramPaymentID PARAMETERS String paramOrganisationID, Long paramPaymentID import java.lang.String; import java.lang.Long")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PayMoneyTransfer extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;

	public static Collection<MoneyTransfer> getChildren(
			PersistenceManager pm, TransferID transferID)
	{
		return getChildren(pm, transferID.organisationID, transferID.transferTypeID, transferID.transferID);
	}

	public static Collection<MoneyTransfer> getChildren(
			PersistenceManager pm, String organisationID, String transferTypeID, long transferID)
	{
		Query query = pm.newQuery(MoneyTransfer.class);
		query.declareImports("import java.lang.String");
		query.declareParameters("String paramOrganisationID, String paramTransferTypeID, long paramTransferID");
		query.setFilter(
				"this.container.organisationID == paramOrganisationID && " +
				"this.container.transferTypeID == paramTransferTypeID && " +
				"this.container.transferID == paramTransferID");
		return (Collection<MoneyTransfer>)query.execute(organisationID, transferTypeID, new Long(transferID));
	}

	/**
	 * This method searches via JDO all <tt>MoneyTransfer</tt>s which have
	 * {@link Transfer#getContainer()}<tt> == this</tt>.
	 */
	public Collection<MoneyTransfer> getChildren()
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
			String organisationID, long paymentID)
	{
		Query query = pm.newNamedQuery(PayMoneyTransfer.class, "getPayMoneyTransferForPayment");
		return (PayMoneyTransfer) query.execute(organisationID, paymentID);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Payment payment;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
	public PayMoneyTransfer(
			Transfer container, User initiator, Anchor from, Anchor to,
			Payment payment)
	{
		super(container, initiator, from, to, payment.getCurrency(),
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
	
	@Override
	protected String internalGetDescription() {
		String invoiceStr = "";
		for (Iterator<Invoice> it = getPayment().getInvoices().iterator(); it.hasNext();) {
			Invoice invoice = it.next();
			invoiceStr = invoiceStr + invoice.getPrimaryKey();
			if (it.hasNext())
				invoiceStr = invoiceStr + ", ";
		}
		return String.format(
				"Payment for invoices: %s",
				invoiceStr
			);
	}
}
