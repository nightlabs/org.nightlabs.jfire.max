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
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * The <code>Storekeeper</code> does a similar job in the store as the
 * {@link org.nightlabs.jfire.accounting.book.Accountant} does in the
 * accounting. This means, it is responsible for local transfers between
 * {@link org.nightlabs.jfire.store.Repository}s when a
 * {@link org.nightlabs.jfire.store.deliver.Delivery} is done.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.book.id.StorekeeperID"
 *		detachable="true"
 *		table="JFireTrade_Storekeeper"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, storekeeperID"
 */
public abstract class Storekeeper
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
	private String storekeeperID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Storekeeper() { }

	public Storekeeper(String organisationID, String storekeeperID)
	{
		this.organisationID = organisationID;
		this.storekeeperID = storekeeperID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getStorekeeperID()
	{
		return storekeeperID;
	}

	public abstract void bookTransfer(User user, LegalEntity mandator, ProductTransfer transfer, Set<Anchor> involvedAnchors);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Storekeeper has no PersistenceManager assigned!");
		return pm;
	}
}
