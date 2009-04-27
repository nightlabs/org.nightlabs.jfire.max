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

package org.nightlabs.jfire.accounting;

import java.util.Collection;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * A {@link MoneyTransfer} is associated to one {@link Invoice}. It is 
 * used for the transfers made when an {@link Invoice} is booked as well as for
 * those transfers made for a {@link Payment}. The property {@link #getBookType()}
 * is then set accordingly to either {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_InvoiceMoneyTransfer"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.query
 * 	name="getInvoiceMoneyTransfersForInvoice"
 * 	query="SELECT
 *			WHERE
 *				this.invoice == :pInvoice &&
 *				this.bookType == :pBookType
 *			"
 * 
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_InvoiceMoneyTransfer")
@Queries(
	@javax.jdo.annotations.Query(
		name="getInvoiceMoneyTransfersForInvoice",
		value="SELECT WHERE this.invoice == :pInvoice && this.bookType == :pBookType ")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class InvoiceMoneyTransfer
extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Book-type set when the transfer is used as sub-transfer for a booking process.
	 */
	public static final String BOOK_TYPE_BOOK = "book";
	/**
	 * Book-type set when the transfer is used as sub-transfer made for a {@link Payment}.
	 */
	public static final String BOOK_TYPE_PAY = "pay";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Invoice invoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String bookType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected InvoiceMoneyTransfer() { }

	private static Currency getCurrency(Invoice invoice)
	{
		if (invoice == null)
			throw new IllegalArgumentException("invoice must not be null!");

		return invoice.getCurrency();
	}

	/**
	 * Create a new {@link InvoiceMoneyTransfer} with no container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param initiator The user that initiated the transfer.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param invoice The {@link Invoice} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public InvoiceMoneyTransfer(
			String bookType,
			User initiator, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(null, initiator, from, to, getCurrency(invoice), amount);
		this.invoice = invoice;
		this.setBookType(bookType);
	}

	/**
	 * Create a new {@link InvoiceMoneyTransfer} for the given container.
	 * 
	 * @param bookType The book-type for the new transfer. The type defines whether the transfer is used for a booking or a payment.
	 * @param containerMoneyTransfer The container-transfer for the new account.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param invoice The {@link Invoice} the new transfer should be linked to.
	 * @param amount The amount of the transfer.
	 */
	public InvoiceMoneyTransfer(
			String bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(containerMoneyTransfer, from, to, amount);
		this.invoice = invoice;
		this.setBookType(bookType);
	}

	/**
	 * @return Returns the invoice this transfer is associated to.
	 */
	public Invoice getInvoice()
	{
		return invoice;
	}

	/**
	 * Set the book-type of this transfer.
	 * 
	 * @param bookType The bookType to set.
	 */
	protected void setBookType(String bookType)
	{
		if (!BOOK_TYPE_BOOK.equals(bookType) &&
				!BOOK_TYPE_PAY.equals(bookType))
			throw new IllegalArgumentException("bookType \""+bookType+"\" is invalid! Must be BOOK_TYPE_BOOK or BOOK_TYPE_PAY!");

		this.bookType = bookType;
	}

	/**
	 * Returns the book-type of this transfer. The book type tells whether the transfer was used during a booking or payment.
	 * The return value should be one of {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 *  
	 * @return The book-type.
	 */
	public String getBookType()
	{
		return bookType;
	}

	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#bookTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		bookTransferAtInvoice(user, involvedAnchors);
		super.bookTransfer(user, involvedAnchors);
	}

	protected void bookTransferAtInvoice(User user, Set<Anchor> involvedAnchors)
	{
		invoice.bookInvoiceMoneyTransfer(this, false);
	}


	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		rollbackTransferAtInvoice(user, involvedAnchors);
		super.rollbackTransfer(user, involvedAnchors);
	}

	protected void rollbackTransferAtInvoice(User user, Set<Anchor> involvedAnchors)
	{
		invoice.bookInvoiceMoneyTransfer(this, true);
	}

	/**
	 * Get those {@link InvoiceMoneyTransfer}s that where made for the given invoice and bookType.
	 * The bookType can be one of the constants in this class {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param invoice The invoice to find the transfers for.
	 * @param bookType The bookType to find the transfers for. Use one of the constants {@link #BOOK_TYPE_BOOK} or {@link #BOOK_TYPE_PAY}.
	 * @return Those {@link InvoiceMoneyTransfer}s that where made for the given invoice and bookType
	 */
	@SuppressWarnings("unchecked")
	public static Collection<InvoiceMoneyTransfer> getInvoiceMoneyTransfers(PersistenceManager pm, Invoice invoice, String bookType) {
		Query q = pm.newNamedQuery(InvoiceMoneyTransfer.class, "getInvoiceMoneyTransfersForInvoice");
		return (Collection<InvoiceMoneyTransfer>) q.execute(invoice, bookType);
	}
}
