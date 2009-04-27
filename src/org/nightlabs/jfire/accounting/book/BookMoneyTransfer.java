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

package org.nightlabs.jfire.accounting.book;


import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * A {@link BookMoneyTransfer} is created when an {@link Invoice} is booked. 
 * It is the container transfer of all sub-transfers made for this booking process. 
 * <p>
 * The transfer happens between two LegalEntities. This means, 
 * <tt>from</tt> and <tt>to</tt> must be instances of <tt>LegalEntity</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.InvoiceMoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_BookMoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_BookMoneyTransfer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BookMoneyTransfer extends InvoiceMoneyTransfer
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(BookMoneyTransfer.class);

	/**
	 * @deprecated Only of JDO!
	 */
	@Deprecated
	protected BookMoneyTransfer() { }

	private static long getAmountAbsoluteValue(Invoice invoice)
	{
		if (invoice == null)
			throw new IllegalArgumentException("invoice must not be null!");

		return invoice.getPrice().getAmountAbsoluteValue();
	}

	/**
	 * BookMoneyTransfer associated to only one Invoice, as it is used for invoice-bookings.
	 *
	 * @param initiator The user that initiated this transfer.
	 * @param from The from-anchor for the new transfer.
	 * @param to The to-anchor for the new transfer.
	 * @param invoice The {@link Invoice} the new transfer should be 
	 */
	public BookMoneyTransfer(User initiator, Anchor from, Anchor to, Invoice invoice)
	{
		super(BOOK_TYPE_BOOK, initiator, from, to, invoice, getAmountAbsoluteValue(invoice));

		if (!(from instanceof LegalEntity))
			throw new IllegalArgumentException("from must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (!(to instanceof LegalEntity))
			throw new IllegalArgumentException("to must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (logger.isDebugEnabled())
			logger.debug("constructor: from=" + from.getPrimaryKey() + " to=" + to.getPrimaryKey());
	}
	
	@Override
	protected String internalGetDescription() {
		return String.format(
				"Booking of invoice %s",
				getInvoice().getPrimaryKey()
			);
	}

}
