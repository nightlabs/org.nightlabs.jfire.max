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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.jfire.store.book.Storekeeper"
 *		detachable = "true"
 *		table="JFireTrade_LocalStorekeeper"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class LocalStorekeeper extends Storekeeper
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected LocalStorekeeper() { }

	public LocalStorekeeper(OrganisationLegalEntity mandator, String storekeeperID)
	{
		super(mandator.getOrganisationID(), storekeeperID);
		this.mandator = mandator;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private OrganisationLegalEntity mandator;

	protected OrganisationLegalEntity getMandator()
	{
		return mandator;
	}

	public void bookTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Map involvedAnchors)
	{
		// A Storekeeper gets all bookings and has to decide himself what to do.
		if (! (transfer instanceof BookProductTransfer))
			return;

		BookProductTransfer bookTransfer = (BookProductTransfer)transfer;		
		DeliveryNote deliveryNote = bookTransfer.getDeliveryNote();

		// find the delegates
		Map delegates = new HashMap();
		for (Iterator iter = deliveryNote.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalStorekeeperDelegate delegate = article.getProductType().getLocalStorekeeperDelegate();
			if (delegate == null) {
				delegate = DefaultLocalStorekeeperDelegate.getDefaultLocalStorekeeperDelegate(getPersistenceManager());
//				throw new IllegalStateException("Could not find LocalStorekeeperDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+".");
				article.getProductType().setLocalStorekeeperDelegate(delegate);
			}
			delegates.put(article, delegate);
		}
		Set distinctDelegates = new HashSet(delegates.values());
		// call preBookDeliveryNote
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) iter.next();
			delegate.preBookArticles(getMandator(), user, deliveryNote, bookTransfer, involvedAnchors);
		}
		// book the individual articles
		for (Iterator iter = deliveryNote.getArticles().iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) delegates.get(article);
			if (delegate == null)
				throw new IllegalStateException("Could not find LocalStorekeeperDelegate for Article "+JDOHelper.getObjectId(article)+" of productType "+JDOHelper.getObjectId(article.getProductType())+", although already resolved prior.");
			// let the delegate do the job
			delegate.bookArticle(getMandator(), user, deliveryNote, article, bookTransfer, involvedAnchors);
		}
		// call postBookDeliveryNote
		for (Iterator iter = distinctDelegates.iterator(); iter.hasNext();) {
			LocalStorekeeperDelegate delegate = (LocalStorekeeperDelegate) iter.next();
			delegate.postBookArticles(getMandator(), user, deliveryNote, bookTransfer, involvedAnchors);
		}
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LocalStorekeeper is not persistent. Can't get PersistenceManager");
		return pm;
	}

}
