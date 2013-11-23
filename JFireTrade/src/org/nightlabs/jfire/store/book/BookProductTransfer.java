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

package org.nightlabs.jfire.store.book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.DeliveryNoteProductTransfer;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.DeliveryNoteProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_BookProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_BookProductTransfer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BookProductTransfer extends DeliveryNoteProductTransfer
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected BookProductTransfer() { }

	private static Collection<Product> getProducts(DeliveryNote deliveryNote, boolean reversing)
	{
		if (deliveryNote == null)
			throw new IllegalArgumentException("deliveryNote must not be null!");

		Collection<? extends Article> articles = deliveryNote.getArticles();
		Collection<Product> products = null;
		for (Article article : articles) {
			if (article.isReversing() == reversing) {
				if (products == null)
					products = new ArrayList<Product>(articles.size());

				products.add(article.getProduct());
			}
		}
		return products;
	}

	/**
	 * This method creates one or two <code>BookProductTransfer</code>s, depending
	 * on whether there are only "normal" articles, only reversing articles or both.
	 *
	 * @param deliveryNote
	 * @return
	 */
	public static List<BookProductTransfer> createBookProductTransfers(User initiator, DeliveryNote deliveryNote)
	{
		if (deliveryNote.getDeliveryNoteLocal().isBooked())
			throw new IllegalArgumentException("deliveryNote is already booked!");

		PersistenceManager pm = JDOHelper.getPersistenceManager(deliveryNote);

		List<BookProductTransfer> res = new ArrayList<BookProductTransfer>(2);
		Collection<Product> productsNormal = getProducts(deliveryNote, false);
		Collection<Product>  productsReversing = getProducts(deliveryNote, true);

		if (productsNormal != null)
			res.add(pm.makePersistent(new BookProductTransfer(
					initiator,
					deliveryNote.getVendor(), deliveryNote.getCustomer(),
					deliveryNote, productsNormal)));

		if (productsReversing != null)
			res.add(pm.makePersistent(new BookProductTransfer(
					initiator,
					deliveryNote.getCustomer(), deliveryNote.getVendor(),
					deliveryNote, productsReversing)));

		return res;
	}

	protected BookProductTransfer(
			User initiator,
			Anchor from, Anchor to, DeliveryNote deliveryNote, Collection<Product> products)
	{
		super(BOOK_TYPE_BOOK, initiator, from, to, deliveryNote, products);

		if (!(from instanceof LegalEntity))
			throw new IllegalArgumentException("from must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (!(to instanceof LegalEntity))
			throw new IllegalArgumentException("to must be an instance of LegalEntity, but is of type " + from.getClass().getName());
	}
	
	@Override
	protected String internalGetDescription() {
		return String.format(
				"Booking of delivery note %s",
				getDeliveryNote().getPrimaryKey()
			);
	}

}
