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

package org.nightlabs.jfire.store;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryNoteProductTransfer extends ProductTransfer
{
	public static final String BOOK_TYPE_BOOK = "book";
	public static final String BOOK_TYPE_DELIVER = "deliver";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryNote deliveryNote;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String bookType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DeliveryNoteProductTransfer() { }

	public DeliveryNoteProductTransfer(
			String bookType,
			User initiator, Anchor from, Anchor to,
			DeliveryNote deliveryNote, Collection<Product> products)
	{
		super((ProductTransfer)null, initiator, from, to, products);

		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		this.deliveryNote = deliveryNote;

		this.setBookType(bookType);
	}

	public DeliveryNoteProductTransfer(
			String bookType,
			Transfer container, Anchor from, Anchor to,
			DeliveryNote deliveryNote, Collection<Product> products)
	{
		super(container, (User)null, from, to, products);

		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		this.deliveryNote = deliveryNote;

		this.setBookType(bookType);
	}

	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}

	/**
	 * @param bookType The bookType to set.
	 */
	protected void setBookType(String bookType)
	{
		if (!BOOK_TYPE_BOOK.equals(bookType) &&
				!BOOK_TYPE_DELIVER.equals(bookType))
			throw new IllegalArgumentException("bookType \""+bookType+"\" is invalid! Must be BOOK_TYPE_BOOK or BOOK_TYPE_DELIVER!");

		this.bookType = bookType;
	}

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
		bookTransferAtDeliveryNote(user, involvedAnchors);
		super.bookTransfer(user, involvedAnchors);
	}

	protected void bookTransferAtDeliveryNote(User user, Set<Anchor> involvedAnchors)
	{
		deliveryNote.bookDeliveryNoteProductTransfer(this, involvedAnchors, false);
	}


	/*
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		rollbackTransferAtDeliveryNote(user, involvedAnchors);
		super.rollbackTransfer(user, involvedAnchors);
	}

	protected void rollbackTransferAtDeliveryNote(User user, Set<Anchor> involvedAnchors)
	{
		deliveryNote.bookDeliveryNoteProductTransfer(this, involvedAnchors, true);
	}
}
