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

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * A {@link MoneyTransfer} is used to describe the transfer of money from one {@link Anchor}
 * to another. When booked by an {@link Account}, its balance will be adjusted accordingly
 * and the {@link MoneyTransfer} will store the Account balance before the transfer was booked. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Transfer"
 *		detachable="true"
 *		table="JFireTrade_MoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="MoneyTransfer.currency" fields="currency"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_MoneyTransfer")
@FetchGroups(
	@FetchGroup(
		name=MoneyTransfer.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MoneyTransfer extends Transfer
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MoneyTransfer.class);

	public static final String TRANSFERTYPEID = "MoneyTransfer";
	
	public static final String FETCH_GROUP_CURRENCY = "MoneyTransfer.currency";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long fromBalanceBeforeTransfer;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long toBalanceBeforeTransfer;
	
	
//	private boolean statistical = false; // necessary?!

	/**
	 * @deprecated Only for JDO! This constructor must never be used directly.
	 */
	@Deprecated
	protected MoneyTransfer() {
	}

	/**
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param currency
	 * @param amount
	 */
	public MoneyTransfer(Transfer container,
			User initiator, Anchor from, Anchor to, Currency currency, long amount)
	{
		super(TRANSFERTYPEID, container, initiator, from, to);

		if (currency == null)
			throw new NullPointerException("currency must not be null!");

		if (amount < 0)
			throw new IllegalArgumentException("amount must be positive! Switch from & to to inverse the direction!");

		this.currency = currency;
		this.amount = amount;

		if (from instanceof Account) {
			Account fromAccount = (Account)from;
			String fromCurrencyID = fromAccount.getCurrency().getCurrencyID();
			if (!currency.getCurrencyID().equals(fromCurrencyID))
				throw new IllegalArgumentException("currency mismatch! From-account \""+from.getPrimaryKey()+"\" has currency \""+fromCurrencyID+"\", but given currency is \""+currency.getCurrencyID()+"\"!");
		}

		if (to instanceof Account) {
			Account toAccount = (Account)to;
			String toCurrencyID = toAccount.getCurrency().getCurrencyID();
			if (!currency.getCurrencyID().equals(toCurrencyID))
				throw new IllegalArgumentException("currency mismatch! To-account \""+to.getPrimaryKey()+"\" has currency \""+toCurrencyID+"\", but given currency is \""+currency.getCurrencyID()+"\"!");
		}
		
		fromBalanceBeforeTransfer = Long.MIN_VALUE;
		toBalanceBeforeTransfer = Long.MIN_VALUE;
	}

//	protected static Collection getSingleInvoiceCollection(Invoice invoice)
//	{
//		ArrayList res = new ArrayList(1);
//		res.add(invoice);
//		return res;
//	}

	/**
	 * Used to create a MoneyTransfer accosiated to
	 * the (first) invoice of containerMoneyTransfer.
	 * 
	 * @param transferRegistry
	 * @param container
	 * @param from
	 * @param to
	 * @param localLegalEntity
	 * @param interLegalEntityMoneyTransfer
	 * @param currency
	 * @param amount
	 */
	public MoneyTransfer(MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			long amount)
	{
		this(containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
				from, to,
				containerMoneyTransfer.getCurrency(),
				amount);
	}

//	/**
//	 * Used to assosiate a MoneyTransfer
//	 * to the given invoice.
//	 *
//	 * @param transferRegistry
//	 * @param container
//	 * @param from
//	 * @param to
//	 * @param localLegalEntity
//	 * @param interLegalEntityMoneyTransfer
//	 * @param currency
//	 * @param amount
//	 */
//	public MoneyTransfer(TransferRegistry transferRegistry,
//			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
//			long amount)
//	{
//		this(transferRegistry, containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
//				from, to,
//				containerMoneyTransfer.getCurrency(),
//				amount);
//	}

	/**
	 * Creates a MoneyTransfer associated to the invoice
	 * of the given containerMoneyTransfer in its currency and with
	 * its price.
	 * 
	 * @param transferRegistry
	 * @param containerMoneyTransfer
	 * @param from
	 * @param to
	 */
	public MoneyTransfer(MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to)
	{
		this(containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
				from, to,
				containerMoneyTransfer.getCurrency(),
				containerMoneyTransfer.getAmount()
				);
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
	
	/**
	 * Get the balance of the from-anchor before the transfer was booked.
	 * @return The balance of the from-anchor before the transfer was booked.
	 */
	public long getFromBalanceBeforeTransfer() {
		return fromBalanceBeforeTransfer;
	}
	
	/**
	 * Get the balance of the to-anchor before the transfer was booked.
	 * @return The balance of the to-anchor before the transfer was booked.
	 */
	public long getToBalanceBeforeTransfer() {
		return toBalanceBeforeTransfer;
	}

	/**
	 * Remember the balance of the from-anchor before the transfer was booked.
	 * This method is called by Account and LegalEntity. The value can only be
	 * set once, attempts to change it will result in an {@link IllegalStateException}.
	 * 
	 * @param fromBalanceBeforeTransfer The balance to set.
	 */
	public void setFromBalanceBeforeTransfer(long fromBalanceBeforeTransfer) {
		if (this.fromBalanceBeforeTransfer == fromBalanceBeforeTransfer)
			return;

		if (this.fromBalanceBeforeTransfer != Long.MIN_VALUE)
			throw new IllegalStateException("fromBalanceBeforeTransfer is immutable and has already been initialised!");

		this.fromBalanceBeforeTransfer = fromBalanceBeforeTransfer;
	}
	
	/**
	 * Remember the balance of the from-anchor before the transfer was booked.
	 * This method is called by Account and LegalEntity. The value can only be
	 * set once, attempts to change it will result in an {@link IllegalStateException}.
	 * 
	 * @param toBalanceBeforeTransfer The balance to set.
	 */
	public void setToBalanceBeforeTransfer(long toBalanceBeforeTransfer) {
		if (this.toBalanceBeforeTransfer == toBalanceBeforeTransfer)
			return;

		if (this.toBalanceBeforeTransfer != Long.MIN_VALUE)
			throw new IllegalStateException("toBalanceBeforeTransfer is immutable and has already been initialised!");

		this.toBalanceBeforeTransfer = toBalanceBeforeTransfer;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation checks if the MoneyTransfer has a container. If so the description
	 * of the container is returned, otherwise as String showing from and to anchor is returned. 
	 * </p>
	 */
	@Override
	protected String internalGetDescription() {
		if (getContainer() != null)
			return getContainer().getDescription();
		return String.format(
				"MoneyTransfer from %s to %s",
				getFrom().getDescription(), getTo().getDescription()
			);
	}

	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors) {
		if (logger.isDebugEnabled()) {
			logger.debug("bookTransfer: this.pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (getFrom() == null ? null : getFrom().getPrimaryKey()) + "\"" + " to.pk=\"" + (getTo() == null ? null : getTo().getPrimaryKey()) + "\" amount=\"" + amount + "\" curreny=\"" + (currency == null ? null : currency.getCurrencyID()) + "\"");
		}
		super.bookTransfer(user, involvedAnchors);
	}
}
