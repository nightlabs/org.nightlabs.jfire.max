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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Anchor"
 *		detachable="true"
 *		table="JFireTrade_Account"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="Account.owner" fields="owner"
 * @jdo.fetch-group name="Account.currency" fields="currency"
 * @jdo.fetch-group name="Account.name" fields="name"
 * @jdo.fetch-group name="Account.shadowAccounts" fields="shadowAccounts"
 * @jdo.fetch-group name="Account.this" fetch-groups="default, Anchor.this" fields="owner, currency, name, shadowAccounts, revenueInAccount, revenueOutAccount"
 */
public class Account extends Anchor
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_OWNER = "Account.owner";
	public static final String FETCH_GROUP_CURRENCY = "Account.currency";
	public static final String FETCH_GROUP_NAME = "Account.name";
	public static final String FETCH_GROUP_SHADOW_ACCOUNTS = "Account.shadowAccounts";
	public static final String FETCH_GROUP_THIS_ACCOUNT = "Account.this";
	
	/**
	 * anchorTypeID for normal accounts of the Local organisation
	 *
	 * @deprecated This type will be deleted very soon. It must be one of
	 *		{@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN}, {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT} or
	 *		{@link #ANCHOR_TYPE_ID_LOCAL_COST} instead!
	 */
	public static final String ANCHOR_TYPE_ID_LOCAL_NORMAL = "Account.Local.Normal";

	/**
	 * anchorTypeID for revenue accounts of the local organisation. This is the default revenue account. In case
	 * money is forwarded to a cost account (see {@link #ANCHOR_TYPE_ID_LOCAL_COST}), a revenue-out account
	 * (see {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT}) is automatically created. This mechanism
	 * allows to keep the system consistent (the sum of all accounts' balances is always 0) while at the same time
	 * having a double entry bookkeeping.
	 * <p>
	 * Note, that even though this is named revenue-<b>in</b> account, a refund will cause money to leave from (i.e. go <b>out</b> of)
	 * this account.
	 * </p>
	 * @see #ANCHOR_TYPE_ID_LOCAL_COST
	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT
	 * @see #setRevenueInAccount(Account)
	 * @see #setRevenueOutAccount(Account)
	 */
	public static final String ANCHOR_TYPE_ID_LOCAL_REVENUE_IN = "Account.Local.Revenue.In";

	/**
	 * anchorTypeID for revenue accounts of the local organisation, but solely as a split account for money that's
	 * forwarded to a cost-account. Revenues will - despite of the name - not be deposited on an account with this type,
	 * but rather on the corresponding revenue-in account.
	 */
	public static final String ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT = "Account.Local.Revenue.Out";

	/**
	 * anchorTypeID for normal accounts of the Local organisation
	 */
	public static final String ANCHOR_TYPE_ID_LOCAL_COST = "Account.Local.Cost";

	/**
	 * anchorTypeID for accounts of trading partners when they acting as vendor
	 */
	public static final String ANCHOR_TYPE_ID_PARTNER_VENDOR = "Account.Partner.Vendor";
	/**
	 * anchorTypeID for accounts of trading partners when they acting as customer
	 */
	public static final String ANCHOR_TYPE_ID_PARTNER_CUSTOMER = "Account.Partner.Customer";

	/**
	 * anchorTypeID for accounts of trading partners when they overpay an i TODO document this!
	 */
	public static final String ANCHOR_TYPE_ID_PARTNER_NEUTRAL = "Account.Partner.Neutral";

	/**
	 * anchorTypeID for accounts that are used during payment. They represent money that's outside
	 * the organisation (means paid to a partner).
	 */
	public static final String ANCHOR_TYPE_ID_OUTSIDE = "Account.Outside";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity owner;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long balance = 0;
//	private Accountant accountant = null;
//	private boolean statistical;

	protected Account() { }

	public Account(String organisationID, String anchorTypeID, String anchorID, LegalEntity owner, Currency currency) {
		super(organisationID, anchorTypeID, anchorID);

		if (currency == null)
			throw new NullPointerException("currency must not be null!");

		this.currency = currency;
		this.owner = owner;
		this.name = new AccountName(this);
		// TODO: What about an accountType (Erlös/Aufwandt)
	}
//	/**
//	 * The accountant who is responsible for MoneyTransfers from and to this Account. The Accountant
//	 * is able to split money and forward the amounts to other accounts.
//	 *
//	 * @return Returns the accountant.
//	 */
//	public Accountant getAccountant()
//	{
//		return accountant;
//	}
	
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="account"
	 */
	protected AccountName name;
	
	public AccountName getName() {
		return name;
	}
	
	
	/**
	 * The balance in the smallest unit available in the Currency of this Account. This is e.g.
	 * Cent for EUR.
	 *
	 * @return Returns the balance.
	 */
	public long getBalance()
	{
		return balance;
	}
	
	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
//	/**
//	 * Whether or not this Account is statistical. If this account is statistical, money that is
//	 * received here is not subtracted from the other "real" account. This means, basically, money
//	 * in a statistical Account does not exist.
//	 *
//	 * @return Returns the statistical.
//	 */
//	public boolean isStatistical()
//	{
//		return statistical;
//	}
	
	protected void adjustBalance(boolean isDebit, long amount) {
		if (isDebit)
			this.balance = this.balance - amount;
		else
			this.balance = this.balance + amount;
	}

	/**
	 * @see org.nightlabs.jfire.transfer.Anchor#internalRollbackTransfer(org.nightlabs.jfire.transfer.Transfer, org.nightlabs.jfire.security.User, java.util.Map)
	 */
	protected void internalRollbackTransfer(Transfer transfer, User user, Map involvedAnchors)
	{
		MoneyTransfer moneyTransfer = (MoneyTransfer) transfer;

		rollbackAccountMoneyTransfer(user, moneyTransfer, involvedAnchors);
	}

	/**
	 * This method is overridden by {@link ShadowAccount}.
	 */
	protected void rollbackAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Map involvedAnchors)
	{
//	 ShadowMoneyTransfers are only stored in ShadowAccounts
		if (! (moneyTransfer instanceof ShadowMoneyTransfer) ) {
			boolean isDebit = Transfer.ANCHORTYPE_TO == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getTo().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
		}
	}

//	public void bookTransfer(User user, Transfer transfer, Map involvedAnchors)
//	{
//		if (transfer == null)
//			throw new NullPointerException("transfer must not be null!");
//
//		if (!(transfer instanceof MoneyTransfer))
//			throw new ClassCastException("Account cannot book another Transfer (\""+transfer.getPrimaryKey()+"\" with type "+transfer.getClass().getName()+") than MoneyTransfer!");
//		
//		MoneyTransfer mt = (MoneyTransfer) transfer;
//
//		if (!mt.getCurrency().getCurrencyID().equals(this.getCurrency().getCurrencyID()))
//			throw new IllegalArgumentException("Attempt to book MoneyTransfer with different currency than currency of this Account!");
//
//		involvedAnchors.put(getPrimaryKey(), this);
//
//		bookAccountMoneyTransfer(user, mt, involvedAnchors);
//	}

	/**
	 * Adjusts the balance according to the Transfer if it is a MoneyTransfer.
	 * 
	 * @see org.nightlabs.jfire.transfer.Anchor#internalBookTransfer(Transfer, User, Map)
	 */
	protected void internalBookTransfer(Transfer transfer, User user, Map<String, Anchor> involvedAnchors)
	{
		MoneyTransfer moneyTransfer = (MoneyTransfer) transfer;

		if (!moneyTransfer.getCurrency().getCurrencyID().equals(this.getCurrency().getCurrencyID()))
			throw new IllegalArgumentException("Attempt to book MoneyTransfer with different currency than currency of this Account!");

		bookAccountMoneyTransfer(user, moneyTransfer, involvedAnchors);
	}

	/**
	 * This method is overridden by {@link ShadowAccount}.
	 */
	protected void bookAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Map involvedAnchors)
	{
//	 ShadowMoneyTransfers are only stored in ShadowAccounts
		if (! (moneyTransfer instanceof ShadowMoneyTransfer) ) {
			boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getFrom().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
//			addTransfer(moneyTransfer);
			
			bookShadowTransfers(user, moneyTransfer, involvedAnchors);
		}
	}
		
	protected void bookShadowTransfers(User user, MoneyTransfer moneyTransfer, Map<String, Anchor> involvedAnchors) {
		Accounting accounting = Accounting.getAccounting(JDOHelper.getPersistenceManager(this));
		boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
		for (Iterator iter = getShadowAccounts().iterator(); iter.hasNext();) {
			ShadowAccount shadowAccount = (ShadowAccount) iter.next();
//			Account from;
//			Account to;
//			if (isDebit) {
//			from = shadowAccount;
//			to = this;
//			}
//			else {
//			to = shadowAccount;
//			from = this;
//			}
//			
			ShadowMoneyTransfer shadowMoneyTransfer = new ShadowMoneyTransfer(
					accounting,
					(InvoiceMoneyTransfer)(moneyTransfer.getContainer() == null ? moneyTransfer : moneyTransfer.getContainer()),
//					from, to,
					(isDebit) ? shadowAccount : this,
					(isDebit) ? this : shadowAccount,
					moneyTransfer.getAmount()
			);
//			JDOHelper.getPersistenceManager(this).makePersistent(shadowMoneyTransfer); // done in constructor
			shadowMoneyTransfer.bookTransfer(user, involvedAnchors);
//			shadowAccount.bookTransfer(user, shadowMoneyTransfer, involvedAnchors);
		}
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ShadowAccount"
	 *		table="JFireTrade_Account_shadowAccounts"
	 *
	 * @jdo.join
	 */
	protected Set shadowAccounts = new HashSet();

	public void addShadowAccount(ShadowAccount shadowAccount) {
		_addShadowAccount(shadowAccount);
		shadowAccount._addShadowedAccount(this);
	}
	
	protected void _addShadowAccount(ShadowAccount shadowAccount) {
		shadowAccounts.add(shadowAccount);
	}
	
	public void removeShadowAccount(ShadowAccount shadowAccount) {
		_removeShadowAccount(shadowAccount);
		shadowAccount._removeShadowedAccount(this);
	}
	
	public void _removeShadowAccount(ShadowAccount shadowAccount) {
		shadowAccounts.remove(shadowAccount);
	}

	public Collection getShadowAccounts() {
		return shadowAccounts;
	}

	public LegalEntity getOwner() {
		return owner;
	}
	
	public void setOwner(LegalEntity owner) {
		this.owner = owner;
	}

	public void checkIntegrity(Collection<Transfer> containers)
	{
		// here we might check later, whether allowed credit limits have been exceeded
	}

	public void resetIntegrity(Collection<Transfer> containers)
	{
	}

	/**
	 * If this Account is the split out account from which money is taken and transferred to a cost center account,
	 * this field will reference the original revenue account (where the money is coming in).
	 *
	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN
	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT
	 * @see #ANCHOR_TYPE_ID_LOCAL_COST
	 *
	 * @jdo.field persistence-modifier="persistent" mapped-by="revenueOutAccount"
	 */
	private Account revenueInAccount = null;

	/**
	 * This is <code>null</code> in most cases. It only references another account (which is automatically
	 * created), if this Account is a revenue account (i.e. getting money for products sold by the local
	 * organisation) and if money continues further to an cost account.
	 *
	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN
	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT
	 * @see #ANCHOR_TYPE_ID_LOCAL_COST
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Account revenueOutAccount = null;

	/**
	 * This method can only be called, if this is a revenue-out account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT}).
	 *
	 * @param revenueInAccount The corresponding revenue-in account. It must have {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN}.
	 */
	protected void setRevenueInAccount(Account revenueInAccount)
	{
		if (revenueInAccount == null)
			throw new IllegalArgumentException("revenueInAccount must not be null!");

		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_IN.equals(revenueInAccount.getAnchorTypeID()))
			throw new IllegalArgumentException("revenueAccountIn \""+revenueInAccount.getPrimaryKey()+"\" has anchorTypeID \"" + revenueInAccount.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_IN + "\"!");

		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT.equals(this.getAnchorTypeID()))
			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT + "\"!");

		if (this.revenueInAccount != null && !this.revenueInAccount.equals(revenueInAccount))
			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has already a revenueInAccount assigned!");

		this.revenueInAccount = revenueInAccount;
	}

	/**
	 * @return Returns <code>null</code>, if this is <b>not</b> a revenue-out account.
	 * @see #setRevenueInAccount(Account)
	 */
	public Account getRevenueInAccount()
	{
		return revenueInAccount;
	}

	/**
	 * This method can only be called, if this is a revenue-in account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN}).
	 *
	 * @param revenueOutAccount The corresponding revenue-out account. It must have {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT}.
	 */
	protected void setRevenueOutAccount(Account revenueOutAccount)
	{
		if (revenueOutAccount == null)
			throw new IllegalArgumentException("revenueOutAccount must not be null!");

		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT.equals(revenueOutAccount.getAnchorTypeID()))
			throw new IllegalArgumentException("revenueAccountOut \""+revenueOutAccount.getPrimaryKey()+"\" has anchorTypeID \"" + revenueOutAccount.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT + "\"!");

		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_IN.equals(this.getAnchorTypeID()))
			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_IN + "\"!");

		if (this.revenueOutAccount != null && !this.revenueOutAccount.equals(revenueOutAccount))
			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has already a revenueOutAccount assigned!");

		this.revenueOutAccount = revenueOutAccount;
	}

	/**
	 * @return Returns <code>null</code>, if this is either <b>not</b> a revenue-in account or if it
	 *		is a revenue-in account but has no revenue-out account assigned, yet.
	 * @see #setRevenueOutAccount(Account)
	 * @see #createRevenueOutAccount()
	 */
	public Account getRevenueOutAccount()
	{
		return revenueOutAccount;
	}

	protected Account _createRevenueOutAccount()
	{
		return new Account(
				this.getOrganisationID(),
				ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT,
				this.getAnchorID(),
				this.getOwner(),
				this.getCurrency());
	}

	/**
	 * This method will create a corresponding revenueOutAccount, if it does not yet exist.
	 * Otherwise, it will return the previously created corresponding revenue-out account.
	 * <p>
	 * This method can only be called, if this is a revenue-in account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_IN}).
	 * </p>
	 *
	 * @return the corresponding revenue-out account.
	 */
	public Account createRevenueOutAccount()
	{
		if (revenueOutAccount == null) {
			if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_IN.equals(this.getAnchorTypeID()))
				throw new IllegalStateException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_IN + "\"!");

			Account account = _createRevenueOutAccount();
			account.setRevenueInAccount(this);
			this.setRevenueOutAccount(account);
		}

		return revenueOutAccount;
	}
}
