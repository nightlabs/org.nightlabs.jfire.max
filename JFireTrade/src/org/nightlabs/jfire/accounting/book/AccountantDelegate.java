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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.id.AccountantDelegateID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * 
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	objectIdClass=AccountantDelegateID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_AccountantDelegate")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class AccountantDelegate implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String accountantDelegateID;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	@Deprecated
	protected AccountantDelegate() { }

	public AccountantDelegate(String organisationID, String accountantDelegateID)
	{
		this.organisationID = organisationID;
		this.accountantDelegateID = accountantDelegateID;
	}

	/**
	 * @return the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return the accountantDelegateID.
	 */
	public String getAccountantDelegateID()
	{
		return accountantDelegateID;
	}

	/**
	 * This method is called by {@link LegalEntity} when it books the given
	 * {@link Transfer} itself and gives this Accountant the opportunity to
	 * perform further action, like creating sub-transfers for the given
	 * one.
	 *
	 * @param user The user that initiated the given transfer.
	 * @param mandator The mandator this accountant acts on behalf of.
	 * @param transfer The transfer to book.
	 * @param involvedAnchors All {@link Anchor}s involved in the booking process.
	 */
	public abstract void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accountantDelegateID == null) ? 0 : accountantDelegateID
						.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountantDelegate other = (AccountantDelegate) obj;
		if (accountantDelegateID == null) {
			if (other.accountantDelegateID != null)
				return false;
		} else if (!accountantDelegateID.equals(other.accountantDelegateID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountantDelegate [accountantDelegateID="
				+ accountantDelegateID + ", organisationID=" + organisationID
				+ "]";
	}
}
