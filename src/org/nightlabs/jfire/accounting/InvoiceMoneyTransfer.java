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

import java.util.Set;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_InvoiceMoneyTransfer"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class InvoiceMoneyTransfer
extends MoneyTransfer
{
	private static final long serialVersionUID = 1L;

	public static final String BOOK_TYPE_BOOK = "book";
	public static final String BOOK_TYPE_PAY = "pay";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Invoice invoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param currency
	 * @param amount
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
	 * @param transferRegistry
	 * @param containerMoneyTransfer
	 * @param from
	 * @param to
	 * @param invoice
	 * @param amount
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
	 * @return Returns the invoice.
	 */
	public Invoice getInvoice()
	{
		return invoice;
	}

	/**
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
	 * @return Returns the bookType.
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
}
