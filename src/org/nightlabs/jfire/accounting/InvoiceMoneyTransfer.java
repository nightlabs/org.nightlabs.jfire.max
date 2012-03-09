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

import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * A {@link MoneyTransfer} is associated to one {@link Invoice}. It is 
 * used for the transfers made when an {@link Invoice} is booked as well as for
 * those transfers made for a {@link Payment}. The property {@link #getBookType()}
 * is then set accordingly to either {@link BookType#book} or {@link BookType#pay}.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(detachable="true")
public class InvoiceMoneyTransfer
	extends PayableObjectMoneyTransfer<Invoice>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected InvoiceMoneyTransfer() { }

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
			BookType bookType,
			User initiator, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(bookType, initiator, from, to, invoice, amount);
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
			BookType bookType,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(bookType, containerMoneyTransfer, from, to, invoice, amount);
	}

	protected void bookTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject)
	{
		Invoice invoice = (Invoice) payableObject;
		invoice.bookInvoiceMoneyTransfer(this, false);
	}

	protected void rollbackTransferAtPayableObject(User user, Set<Anchor> involvedAnchors, PayableObject payableObject)
	{
		Invoice invoice = (Invoice) payableObject;
		invoice.bookInvoiceMoneyTransfer(this, true);
	}
}
