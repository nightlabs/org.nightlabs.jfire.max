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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.id.AccountantID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * An Accountant is responsible for splitting money into several accounts and for
 * vetoing if a money transfer cannot be done. One Accountant can be responsible for an
 * undefinite number of accounts or for only one. The main job of the Accountant is
 * to split amounts e.g. into certain taxes.
 * <p>
 * The entry point for an accountants work is the {@link #bookTransfer(User, LegalEntity, MoneyTransfer, Set)}
 * method that is called by {@link LegalEntity} when it books a Transfer itself (Each {@link LegalEntity} has
 * an {@link Accountant} assigned).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	objectIdClass=AccountantID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Accountant")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Accountant implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PARTNER_ACCOUTANT_ID = "PartnerAccountant";
	public static final String LOCAL_ACCOUNTANT_ID = "LocalAccountant";
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String accountantID;

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_Accountant_moneyTransferClass2accountantDelegate",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private Map<String, AccountantDelegate> moneyTransferClass2accountantDelegate;
	
	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	@Deprecated
	protected Accountant() { }

	public Accountant(String organisationID, String accountantID)
	{
		this.organisationID = organisationID;
		this.accountantID = accountantID;
		
		this.moneyTransferClass2accountantDelegate = new HashMap<String, AccountantDelegate>();
	}

	/**
	 * @return the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return the accountantID.
	 */
	public String getAccountantID()
	{
		return accountantID;
	}

//	/**
//	 * This method is called by {@link LegalEntity} when it books the given
//	 * {@link Transfer} itself and gives this Accountant the opportunity to
//	 * perform further action, like creating sub-transfers for the given
//	 * one.
//	 *
//	 * @param user The user that initiated the given transfer.
//	 * @param mandator The mandator this accountant acts on behalf of.
//	 * @param transfer The transfer to book.
//	 * @param involvedAnchors All {@link Anchor}s involved in the booking process.
//	 */
//	public abstract void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors);
	
	public void setAccountantDelegate(Class<? extends MoneyTransfer> moneyTransferClass, AccountantDelegate accountantDelegate) {
		this.moneyTransferClass2accountantDelegate.put(moneyTransferClass.getName(), accountantDelegate);
	}

	/**
	 * Returns an {@link AccountantDelegate} registered for this
	 * {@link Accountant} to handle either the given type of MoneyTransfer or
	 * any of its super-types (including interfaces).
	 * 
	 * @param moneyTransferClass
	 *            The type of MoneyTransfer to search a delegate for. Note, that
	 *            super-types are included in the search.
	 * @return The {@link AccountantDelegate} registered to handle the given
	 *         type of MoneyTransfer or one of its super-types.
	 */
	public AccountantDelegate getAccountantDelegate(Class<? extends MoneyTransfer> moneyTransferClass) {
		List<Class<?>> moneyTransferTypeHeirarchy = ReflectUtil.collectTypeHierarchy(moneyTransferClass);
		for (Class<?> transferClass : moneyTransferTypeHeirarchy) {
			AccountantDelegate delegate = moneyTransferClass2accountantDelegate.get(transferClass.getName());
			if (delegate != null) {
				return delegate;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accountantID == null) ? 0 : accountantID.hashCode());
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
		Accountant other = (Accountant) obj;
		if (accountantID == null) {
			if (other.accountantID != null)
				return false;
		} else if (!accountantID.equals(other.accountantID))
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
		return "Accountant [accountantID=" + accountantID + ", organisationID="
				+ organisationID + "]";
	}
}