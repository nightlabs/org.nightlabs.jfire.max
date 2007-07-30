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

import java.io.Serializable;
import java.util.Set;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * An Accountant is responsible for splitting money into several accounts and for
 * vetoing if a money transfer cannot be done. One Accountant can be responsible for an
 * undefinite number of Account-s or for only one. The main job of the Accountant is
 * to split amounts e.g. into certain taxes.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.AccountantID"
 *		detachable="true"
 *		table="JFireTrade_Accountant"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, accountantID"
 */
public abstract class Accountant implements Serializable
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
	private String accountantID;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	protected Accountant() { }

	public Accountant(String organisationID, String accountantID)
	{
		this.organisationID = organisationID;
		this.accountantID = accountantID;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}	
	/**
	 * @return Returns the accountantID.
	 */
	public String getAccountantID()
	{
		return accountantID;
	}
	public abstract void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors);
}
