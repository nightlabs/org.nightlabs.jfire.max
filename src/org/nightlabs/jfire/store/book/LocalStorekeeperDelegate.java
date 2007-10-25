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

import java.io.Serializable;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.book.id.LocalStorekeeperDelegateID"
 *		detachable="true"
 *		table="JFireTrade_LocalStorekeeperDelegate"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, localStorekeeperDelegateID"
 */
public abstract class LocalStorekeeperDelegate
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localStorekeeperDelegateID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected LocalStorekeeperDelegate() { }

	public LocalStorekeeperDelegate(String organisationID, String localStorekeeperDelegateID)
	{
		this.organisationID = organisationID;
		this.localStorekeeperDelegateID = localStorekeeperDelegateID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getLocalStorekeeperDelegateID()
	{
		return localStorekeeperDelegateID;
	}

	/**
	 * Book the given article.
	 * A LocalStorekeeperDelegate should decide based on its
	 * configuration to which repositories the Products shall be booked.
	 * @param mandator The organisation the LocalStorekeeper books for.
	 * @param user The user that initiated the booking.
	 * @param deliveryNote The deliveryNote that is currently booked.
	 * @param article The Article to book.
	 * @param container The Container transfer, that is the transfer from the customer to the vendor of the deliveryNote 
	 * @param involvedAnchors A List of involved Anchors, so they can be checked after the booking
	 */
	public abstract void bookArticle(
			OrganisationLegalEntity mandator, 
			User user,
			DeliveryNote deliveryNote,
			Article article, 
			BookProductTransfer container, 
			Set<Anchor> involvedAnchors
		);

	/**
	 * Called by LocalStorekeeper before all articles of a deliveryNote are booked.
	 * Gives the delegate the chance to initialize.
	 * @param bookTransfer TODO
	 * @param involvedAnchors TODO
	 */
	public void preBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Set<Anchor> involvedAnchors) {}

	/**
	 * Called by LocalStorekeeper before all articles of a deliveryNote are booked.
	 * Gives the delegate the chance to clean up.
	 * @param bookTransfer TODO
	 * @param involvedAnchors TODO
	 */
	public void postBookArticles(OrganisationLegalEntity mandator, User user, DeliveryNote deliveryNote, BookProductTransfer bookTransfer, Set<Anchor> involvedAnchors) {}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Currently not persistent or attached to datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * Convenience method to obtain the store.
	 */
	protected Store getStore()
	{
		return Store.getStore(getPersistenceManager());
	}
}
