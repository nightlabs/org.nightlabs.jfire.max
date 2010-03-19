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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Queries;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.Account"
 *		detachable="true"
 *		table="JFireTrade_SummaryAccount"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="SummaryAccount.summedAccounts" fields="summedAccounts"
 * @jdo.fetch-group name="SummaryAccount.this" fetch-groups="default, Account.this" fields="summedAccounts"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_SummaryAccount")
@FetchGroups({
	@FetchGroup(
		name=SummaryAccount.FETCH_GROUP_SUMMED_ACCOUNTS,
		members=@Persistent(name="summedAccounts")),
	@FetchGroup(
		fetchGroups={"default", "Account.this"},
		name=SummaryAccount.FETCH_GROUP_THIS_SUMMARY_ACCOUNT,
		members=@Persistent(name="summedAccounts"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries(
	@javax.jdo.annotations.Query(
		name="getAllSummaryAccountsForAccount",
		value="SELECT WHERE this.summedAccounts.contains(:account)")
)
public class SummaryAccount extends Account
{
	private static final long serialVersionUID = 1L;

//	/**
//	 * anchorTypeID for {@link SummaryAccount}s
//	 */
//	public static final String ANCHOR_TYPE_ID_SUMMARY = "Account.Summary";

	public static final String FETCH_GROUP_SUMMED_ACCOUNTS = "SummaryAccount.summedAccounts";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_SUMMARY_ACCOUNT = "SummaryAccount.this";

	public static Collection<SummaryAccount> getAllSummaryAccountsForAccount(PersistenceManager pm, Account account)
	{
		Query q = pm.newNamedQuery(SummaryAccount.class, "getAllSummaryAccountsForAccount");
		return (Collection<SummaryAccount>) q.execute(account);
	}
	
	private static final Logger logger = Logger.getLogger(SummaryAccount.class);
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SummaryAccount() {
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Account"
	 *		table="JFireTrade_SummaryAccount_summedAccounts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_SummaryAccount_summedAccounts",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Set<Account> summedAccounts;

	public void addSummedAccount(Account account) {
		_addSummedAccount(account);
		account._addSummaryAccount(this);
	}

	protected void _addSummedAccount(Account account) {
		summedAccounts.add(account);
	}

	public void removeSummedAccount(Account account) {
		_removeSummedAccount(account);
		account._removeSummaryAccount(this);
	}

	public void _removeSummedAccount(Account account) {
		summedAccounts.remove(account);
	}

	public Collection<Account> getSummedAccounts() {
		return Collections.unmodifiableCollection(summedAccounts);
	}

//	@SuppressWarnings("unchecked")
//	public void clearSummedAccounts() {
//		for (Iterator it = new ArrayList(summedAccounts).iterator(); it.hasNext();) {
//			Account account = (Account) it.next();
//			removeSummedAccount(account);
//		}
//	}

	public SummaryAccount(
			String organisationID,
			String anchorID,
			AccountType accountType,
			LegalEntity owner,
			Currency currency)
	{
		super(organisationID, anchorID, accountType, owner, currency);
		summedAccounts = new HashSet<Account>();
	}

	@Override
	protected void rollbackAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Set<Anchor> involvedAnchors)
	{
		if (moneyTransfer instanceof SummaryMoneyTransfer) {
			boolean isDebit = Transfer.ANCHORTYPE_TO == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getTo().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
		}
		else
			throw new IllegalStateException("moneyTransfer is not an instance of SummaryMoneyTransfer!"); // can this happen?
	}

	/**
	 * @see org.nightlabs.jfire.accounting.Account#bookAccountMoneyTransfer(org.nightlabs.jfire.security.User, org.nightlabs.jfire.accounting.MoneyTransfer, java.util.Map)
	 */
	@Override
	protected void bookAccountMoneyTransfer(User user,
			MoneyTransfer moneyTransfer, Set<Anchor> involvedAnchors)
	{
		if (skip_bookAccountMoneyTransfer)
			return;

//	 only SummaryMoneyTransfers are stored in SummaryAccounts
		if (moneyTransfer instanceof SummaryMoneyTransfer) {
			boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getFrom().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
//			addTransfer(moneyTransfer);
			// book summary transfers to SummaryAccounts
			bookSummaryTransfers(user, moneyTransfer, involvedAnchors);
		}
		else
			throw new IllegalStateException("moneyTransfer is not an instance of SummaryMoneyTransfer!"); // can this happen?
	}

	@Override
	public void jdoPostAttach(Object object) {
		super.jdoPostAttach(object);
		SummaryAccount attached = this;
		PersistenceManager pm = getPersistenceManager();
		// retrieve all summed accounts where this account is registered as summary account
		Collection<Account> summedAccounts = Account.getAllSummedAccountsForSummaryAccount(pm, attached);
		
		// we check if the data is consistent 
		if (!attached.summedAccounts.equals(summedAccounts)) {
			// if a summed account contains this account as summary account although it is NOT registered here anymore remove it from the summed account
			for (Account summedAccount : summedAccounts) {
				if (summedAccount.summaryAccounts.contains(attached) && !attached.summedAccounts.contains(summedAccount)) {
					summedAccount._removeSummaryAccount(attached);
					if (logger.isDebugEnabled()) {
						logger.debug("jdoPostAttach: Removed summary account "+attached+" from summed account "+summedAccount+" to maintain consistency");
					}
				}
			}
			// check if all summed account data is correct (consistent)
			for (Account summedAccount : attached.summedAccounts) {
				if (!summedAccounts.contains(summedAccount)) {
					// if we find a summary account where this account is NOT registered as summed account in a summary account add it 
					summedAccount._addSummaryAccount(attached);
					if (logger.isDebugEnabled()) {
						logger.debug("jdoPostAttach: Added summary account "+attached+" to summed account "+summedAccount+" to maintain consistency");
					}					
				}
			}			
		}
	}
}
