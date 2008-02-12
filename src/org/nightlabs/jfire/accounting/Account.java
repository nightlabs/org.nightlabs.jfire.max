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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

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
 * @jdo.fetch-group name="Account.summaryAccounts" fields="summaryAccounts"
 * @jdo.fetch-group name="Account.accountType" fields="accountType"
 * @jdo.fetch-group name="Account.this" fetch-groups="default, Anchor.this" fields="owner, currency, accountType, name, summaryAccounts"
 *
 * @jdo.query
 *		name="getAccountsForAccountTypeAndOwnerAndCurrency"
 *		query="SELECT
 *				WHERE this.accountType == :accountType && this.owner == :owner && this.currency == :currency"
 */
public class Account extends Anchor
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_OWNER = "Account.owner";
	public static final String FETCH_GROUP_CURRENCY = "Account.currency";
	public static final String FETCH_GROUP_NAME = "Account.name";
	public static final String FETCH_GROUP_SUMMARY_ACCOUNTS = "Account.summaryAccounts";
	public static final String FETCH_GROUP_ACCOUNT_TYPE = "Account.accountType";
	public static final String FETCH_GROUP_THIS_ACCOUNT = "Account.this";

	@SuppressWarnings("unchecked")
	public static Collection<? extends Account> getAccounts(PersistenceManager pm, AccountType accountType, LegalEntity owner, Currency currency)
	{
		Query q = pm.newNamedQuery(Account.class, "getAccountsForAccountTypeAndOwnerAndCurrency");
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("accountType", accountType);
		params.put("owner", owner);
		params.put("currency", currency);
		return (Collection<? extends Account>) q.executeWithMap(params);
	}

////	/**
////	 * anchorTypeID for normal accounts of the Local organisation
////	 *
////	 * @deprecated This type will be deleted very soon. It must be one of
////	 *		{@link #ANCHOR_TYPE_ID_LOCAL_REVENUE}, {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT} or
////	 *		{@link #ANCHOR_TYPE_ID_LOCAL_EXPENSE} instead!
////	 */
////	public static final String ANCHOR_TYPE_ID_LOCAL_NORMAL = "Account.Local.Normal";
//
//	/**
//	 * anchorTypeID for revenue accounts of the local organisation.
//	 *
//	 * @see #ANCHOR_TYPE_ID_LOCAL_EXPENSE
//	 * @see #setRevenueInAccount(Account)
//	 * @see #setRevenueOutAccount(Account)
//	 */
//	public static final String ANCHOR_TYPE_ID_LOCAL_REVENUE = "Account.Local.Revenue";
////	public static final String ANCHOR_TYPE_ID_LOCAL_REVENUE_IN = "Account.Local.Revenue.In";
//
////	/**
////	 * anchorTypeID for revenue accounts of the local organisation, but solely as a split account for money that's
////	 * forwarded to a cost-account. Revenues will - despite of the name - not be deposited on an account with this type,
////	 * but rather on the corresponding revenue-in account.
////	 */
////	public static final String ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT = "Account.Local.Revenue.Out";
//
//	/**
//	 * anchorTypeID for expense accounts of the Local organisation
//	 */
//	public static final String ANCHOR_TYPE_ID_LOCAL_EXPENSE = "Account.Local.Expense";
//
//	/**
//	 * anchorTypeID for accounts of trading partners when they acting as vendor
//	 */
//	public static final String ANCHOR_TYPE_ID_PARTNER_VENDOR = "Account.Partner.Vendor";
//	
//	/**
//	 * anchorTypeID for accounts of trading partners when they acting as customer
//	 */
//	public static final String ANCHOR_TYPE_ID_PARTNER_CUSTOMER = "Account.Partner.Customer";
//
//	/**
//	 * anchorTypeID for accounts of trading partners when they overpay multiple invoices and
//	 * it cannot be determined whether the partner is a customer or a vendor.
//	 */
//	public static final String ANCHOR_TYPE_ID_PARTNER_NEUTRAL = "Account.Partner.Neutral";
//
//	/**
//	 * anchorTypeID for accounts that are used during payment. They represent money that's outside
//	 * the organisation (means paid to a partner), hence their {@link #isOutside()} property is <code>true</code>.
//	 */
//	public static final String ANCHOR_TYPE_ID_OUTSIDE = "Account.Outside";
////	public static final String ANCHOR_TYPE_ID_PAYMENT = "Account.Payment";
	
	/**
	 * @jdo.field persistence-modifier="persistent" 
	 */
	private AccountType accountType;

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

//	/**
//	 * Whether or not this Repository represents sth. outside.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean outside;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Account() { }

	public static final String ANCHOR_TYPE_ID_ACCOUNT = "Account";

	public Account(String organisationID, String anchorID, AccountType accountType, LegalEntity owner, Currency currency) {
		super(organisationID, ANCHOR_TYPE_ID_ACCOUNT, anchorID);

		if (accountType == null)
			throw new IllegalArgumentException("accountType must not be null!");

		if (owner == null)
			throw new IllegalArgumentException("owner must not be null!");

		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		this.accountType = accountType;
		this.currency = currency;
		this.owner = owner;
		this.name = new AccountName(this);
		this.summaryAccounts = new HashSet<SummaryAccount>();
//		this.outside = outside;
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

	public AccountType getAccountType()
	{
		return accountType;
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
	@Override
	protected void internalRollbackTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		MoneyTransfer moneyTransfer = (MoneyTransfer) transfer;

		rollbackAccountMoneyTransfer(user, moneyTransfer, involvedAnchors);
	}

	/**
	 * This method is overridden by {@link SummaryAccount}.
	 */
	protected void rollbackAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Set<Anchor> involvedAnchors)
	{
//	 SummaryMoneyTransfers are only stored in SummaryAccounts
		if (! (moneyTransfer instanceof SummaryMoneyTransfer) ) {
			boolean isDebit = Transfer.ANCHORTYPE_TO == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getTo().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
		}
	}

//	public void bookTransfer(User user, Transfer transfer, Set<Anchor> involvedAnchors)
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
	@Override
	protected void internalBookTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		MoneyTransfer moneyTransfer = (MoneyTransfer) transfer;

		if (!moneyTransfer.getCurrency().getCurrencyID().equals(this.getCurrency().getCurrencyID()))
			throw new IllegalArgumentException("Attempt to book MoneyTransfer with different currency than currency of this Account!");

		bookAccountMoneyTransfer(user, moneyTransfer, involvedAnchors);
	}

	/**
	 * This method is overridden by {@link SummaryAccount}.
	 */
	protected void bookAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Set<Anchor> involvedAnchors)
	{
		if (skip_bookAccountMoneyTransfer)
			return;

//	 SummaryMoneyTransfers are only stored in SummaryAccounts
		if (! (moneyTransfer instanceof SummaryMoneyTransfer) ) {
			boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getFrom().getPrimaryKey().equals(this.getPrimaryKey());
			if (isTransferFrom(moneyTransfer)) {
				moneyTransfer.setFromBalanceBeforeTransfer(getBalance());
			} else if (isTransferTo(moneyTransfer)) {
				moneyTransfer.setToBalanceBeforeTransfer(getBalance());
			}
			adjustBalance(isDebit, moneyTransfer.getAmount());
//			addTransfer(moneyTransfer);

			bookSummaryTransfers(user, moneyTransfer, involvedAnchors);
		}
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient boolean skip_bookAccountMoneyTransfer = false;

	protected void bookSummaryTransfers(User user, MoneyTransfer moneyTransfer, Set<Anchor> involvedAnchors) {
		skip_bookAccountMoneyTransfer = true;
		try {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
			for (Iterator iter = getSummaryAccounts().iterator(); iter.hasNext();) {
				SummaryAccount summaryAccount = (SummaryAccount) iter.next();
	
	//			Account from;
	//			Account to;
	//			if (isDebit) {
	//			from = SummaryAccount;
	//			to = this;
	//			}
	//			else {
	//			to = SummaryAccount;
	//			from = this;
	//			}
	
				SummaryMoneyTransfer summaryMoneyTransfer = new SummaryMoneyTransfer(
						(InvoiceMoneyTransfer)(moneyTransfer.getContainer() == null ? moneyTransfer : moneyTransfer.getContainer()),
	//					from, to,
						(isDebit) ? summaryAccount : this,
						(isDebit) ? this : summaryAccount,
						moneyTransfer.getAmount()
				);
				summaryMoneyTransfer = pm.makePersistent(summaryMoneyTransfer);
	//			JDOHelper.getPersistenceManager(this).makePersistent(summaryMoneyTransfer); // done in constructor
				summaryMoneyTransfer.bookTransfer(user, involvedAnchors);
	//			SummaryAccount.bookTransfer(user, summaryMoneyTransfer, involvedAnchors);
			}
		} finally {
			skip_bookAccountMoneyTransfer = false;
		}
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="SummaryAccount"
	 *		table="JFireTrade_Account_summaryAccounts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Set<SummaryAccount> summaryAccounts;

	public void addSummaryAccount(SummaryAccount summaryAccount) {
		_addSummaryAccount(summaryAccount);
		summaryAccount._addSummedAccount(this);
	}
	
	protected void _addSummaryAccount(SummaryAccount summaryAccount) {
		summaryAccounts.add(summaryAccount);
	}
	
	public void removeSummaryAccount(SummaryAccount summaryAccount) {
		_removeSummaryAccount(summaryAccount);
		summaryAccount._removeSummedAccount(this);
	}
	
	public void _removeSummaryAccount(SummaryAccount summaryAccount) {
		summaryAccounts.remove(summaryAccount);
	}

	public Collection<SummaryAccount> getSummaryAccounts() {
		return Collections.unmodifiableCollection(summaryAccounts);
	}

//	@SuppressWarnings("unchecked")
//	public void clearSummaryAccounts() {
//		for (Iterator it = new ArrayList(summaryAccounts).iterator(); it.hasNext();) {
//			SummaryAccount sa = (SummaryAccount) it.next();
//			removeSummaryAccount(sa);
//		}
//	}

	public LegalEntity getOwner() {
		return owner;
	}
	
	public void setOwner(LegalEntity owner) {
		this.owner = owner;
	}

	@Override
	public void checkIntegrity(Collection<? extends Transfer> containers)
	{
		// here we might check later, whether allowed credit limits have been exceeded
	}

	@Override
	public void resetIntegrity(Collection<? extends Transfer> containers)
	{
	}
	
	@Override
	public String getDescription(Locale locale) {
		return getName().getText(locale.getLanguage());
	}

//	/**
//	 * If this Account is the split out account from which money is taken and transferred to a cost center account,
//	 * this field will reference the original revenue account (where the money is coming in).
//	 * <p>
//	 * This means that if this Account is the revenueOutAccount of an account, this member will point back to that Account.
//	 * </p>
//	 *
//	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE
//	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT
//	 * @see #ANCHOR_TYPE_ID_LOCAL_EXPENSE
//	 *
//	 * @jdo.field persistence-modifier="persistent" mapped-by="revenueOutAccount"
//	 */
//	private Account revenueInAccount = null;

//	/**
//	 * This is <code>null</code> in most cases. It only references another account (which is automatically
//	 * created), if this Account is a revenue account (i.e. getting money for products sold by the local
//	 * organisation) and if money continues further to an cost account.
//	 *
//	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE
//	 * @see #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT
//	 * @see #ANCHOR_TYPE_ID_LOCAL_EXPENSE
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Account revenueOutAccount = null;
//
//	/**
//	 * This method can only be called, if this is a revenue-out account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT}).
//	 *
//	 * @param revenueInAccount The corresponding revenue-in account. It must have {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE}.
//	 */
//	protected void setRevenueInAccount(Account revenueInAccount)
//	{
//		if (revenueInAccount == null)
//			throw new IllegalArgumentException("revenueInAccount must not be null!");
//
//		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE.equals(revenueInAccount.getAnchorTypeID()))
//			throw new IllegalArgumentException("revenueAccountIn \""+revenueInAccount.getPrimaryKey()+"\" has anchorTypeID \"" + revenueInAccount.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE + "\"!");
//
//		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT.equals(this.getAnchorTypeID()))
//			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT + "\"!");
//
//		if (this.revenueInAccount != null && !this.revenueInAccount.equals(revenueInAccount))
//			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has already a revenueInAccount assigned!");
//
//		this.revenueInAccount = revenueInAccount;
//	}

//	/**
//	 * @return Returns <code>null</code>, if this is <b>not</b> a revenue-out account.
//	 * @see #setRevenueInAccount(Account)
//	 */
//	public Account getRevenueInAccount()
//	{
//		return revenueInAccount;
//	}

//	/**
//	 * This method can only be called, if this is a revenue-in account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE}).
//	 *
//	 * @param revenueOutAccount The corresponding revenue-out account. It must have {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT}.
//	 */
//	protected void setRevenueOutAccount(Account revenueOutAccount)
//	{
//		if (revenueOutAccount == null)
//			throw new IllegalArgumentException("revenueOutAccount must not be null!");
//
//		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT.equals(revenueOutAccount.getAnchorTypeID()))
//			throw new IllegalArgumentException("revenueAccountOut \""+revenueOutAccount.getPrimaryKey()+"\" has anchorTypeID \"" + revenueOutAccount.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT + "\"!");
//
//		if (!ANCHOR_TYPE_ID_LOCAL_REVENUE.equals(this.getAnchorTypeID()))
//			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE + "\"!");
//
//		if (this.revenueOutAccount != null && !this.revenueOutAccount.equals(revenueOutAccount))
//			throw new IllegalArgumentException("this \""+this.getPrimaryKey()+"\" has already a revenueOutAccount assigned!");
//
//		this.revenueOutAccount = revenueOutAccount;
//	}

//	/**
//	 * @return Returns <code>null</code>, if this is either <b>not</b> a revenue-in account or if it
//	 *		is a revenue-in account but has no revenue-out account assigned, yet.
//	 * @see #setRevenueOutAccount(Account)
//	 * @see #createRevenueOutAccount()
//	 */
//	public Account getRevenueOutAccount()
//	{
//		return revenueOutAccount;
//	}
//
//	protected Account _createRevenueOutAccount()
//	{
//		return new Account(
//				this.getOrganisationID(),
//				ANCHOR_TYPE_ID_LOCAL_REVENUE_OUT,
//				this.getAnchorID(),
//				this.getOwner(),
//				this.getCurrency(),
//				false);
//	}

//	/**
//	 * This method will create a corresponding revenueOutAccount, if it does not yet exist.
//	 * Otherwise, it will return the previously created corresponding revenue-out account.
//	 * <p>
//	 * This method can only be called, if this is a revenue-in account (i.e. having {@link #ANCHOR_TYPE_ID_LOCAL_REVENUE}).
//	 * </p>
//	 *
//	 * @return the corresponding revenue-out account.
//	 */
//	public Account createRevenueOutAccount()
//	{
//		if (revenueOutAccount == null) {
//			if (!ANCHOR_TYPE_ID_LOCAL_REVENUE.equals(this.getAnchorTypeID()))
//				throw new IllegalStateException("this \""+this.getPrimaryKey()+"\" has anchorTypeID \"" + this.getAnchorTypeID() + "\" which is illegal! It must be \"" + ANCHOR_TYPE_ID_LOCAL_REVENUE + "\"!");
//
//			Account account = _createRevenueOutAccount();
//			account.setRevenueInAccount(this);
//			this.setRevenueOutAccount(account);
//		}
//
//		return revenueOutAccount;
//	}

//	public boolean isOutside()
//	{
//		return outside;
//	}
}
