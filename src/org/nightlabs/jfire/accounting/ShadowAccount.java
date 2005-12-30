/*
 * Created 	on Mar 8, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.Account"
 *		detachable="true"
 *		table="JFireTrade_ShadowAccount"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ShadowAccount.shadowedAccounts" fields="shadowedAccounts"
 * @jdo.fetch-group name="ShadowAccount.this" fetch-groups="default, Account.this" fields="shadowedAccounts"
 */
public class ShadowAccount extends Account
{

	/**
	 * anchorTypeID for shadow accounts of the Local organisation
	 */
	public static final String ANCHOR_TYPE_ID_LOCAL_SHADOW = "LocalAccount.Shadow";

	public static final String FETCH_GROUP_SHADOWED_ACCOUNTS = "ShadowAccount.shadowedAccounts";
	public static final String FETCH_GROUP_THIS_SHADOW_ACCOUNT = "ShadowAccount.this";
	

	protected ShadowAccount() {
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Account"
	 *		table="JFireTrade_ShadowAccount_shadowedAccounts"
	 *
	 * @jdo.join
	 */
	protected Set shadowedAccounts = new HashSet();

	public void addShadowedAccount(Account account) {
		_addShadowedAccount(account);
		account._addShadowAccount(this);
	}
	
	protected void _addShadowedAccount(Account account) {
		shadowedAccounts.add(account);
	}
	
	public void removeShadowedAccount(Account account) {
		_removeShadowedAccount(account);
		account._removeShadowAccount(this);
	}
	
	public void _removeShadowedAccount(Account account) {
		shadowedAccounts.remove(account);
	}

	public Collection getShadowedAccounts() {
		return shadowedAccounts;
	}

	/**
	 * @param organisationID
	 * @param anchorID
	 * @param currency
	 * @param statistical
	 */
	public ShadowAccount(String organisationID, String anchorID,
			LegalEntity owner, Currency currency) {
		super(organisationID, ANCHOR_TYPE_ID_LOCAL_SHADOW, anchorID, owner, currency);
	}

	protected void rollbackAccountMoneyTransfer(User user, MoneyTransfer moneyTransfer, Map involvedAnchors)
	{
		if (moneyTransfer instanceof ShadowMoneyTransfer) {
			boolean isDebit = Transfer.ANCHORTYPE_TO == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getTo().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
		}
		else
			throw new IllegalStateException("moneyTransfer is not an instance of ShadowMoneyTransfer!"); // can this happen?
	}

	/**
	 * @see org.nightlabs.jfire.accounting.Account#bookAccountMoneyTransfer(org.nightlabs.jfire.security.User, org.nightlabs.jfire.accounting.MoneyTransfer, java.util.Map)
	 */
	protected void bookAccountMoneyTransfer(User user,
			MoneyTransfer moneyTransfer, Map involvedAnchors)
	{
//	 only ShadowMoneyTransfers are stored in ShadowAccounts
		if (moneyTransfer instanceof ShadowMoneyTransfer) {
			boolean isDebit = Transfer.ANCHORTYPE_FROM == moneyTransfer.getAnchorType(this);
//			boolean isDebit = moneyTransfer.getFrom().getPrimaryKey().equals(this.getPrimaryKey());
			adjustBalance(isDebit, moneyTransfer.getAmount());
//			addTransfer(moneyTransfer);
			// book shadow transfers to shadowing accounts
			bookShadowTransfers(user, moneyTransfer, involvedAnchors);
		}
		else
			throw new IllegalStateException("moneyTransfer is not an instance of ShadowMoneyTransfer!"); // can this happen?
	}

}
