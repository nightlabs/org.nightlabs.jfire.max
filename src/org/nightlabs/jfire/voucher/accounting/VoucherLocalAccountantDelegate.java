package org.nightlabs.jfire.voucher.accounting;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * {@link VoucherLocalAccountantDelegate} is assigned to ProductTypes of type
 * {@link VoucherType}. It directs money from/to an account defined for
 * the delegate. Account for this delegate are defined per currency.
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.LocalAccountantDelegate"
 *		detachable="true"
 *		table="JFireVoucher_VoucherLocalAccountantDelegate"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="VoucherLocalAccountantDelegate.accounts" fields="accounts"
 *
 * @!jdo.query
 *		name="getVoucherLocalAccountantDelegateByAccount"
 *		query="SELECT UNIQUE WHERE this.account == :account"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherLocalAccountantDelegate")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=VoucherLocalAccountantDelegate.FETCH_GROUP_VOUCHER_LOCAL_ACCOUNTS,
		members=@Persistent(name="accounts"))
})
public class VoucherLocalAccountantDelegate
extends LocalAccountantDelegate
{
	private static final long serialVersionUID = 1L;

//	public static final String ACCOUNT_ANCHOR_TYPE_ID_VOUCHER = "Account.Voucher";

	public static final String FETCH_GROUP_VOUCHER_LOCAL_ACCOUNTS = "VoucherLocalAccountantDelegate.accounts";

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="Account"
	 *		table="JFireVoucher_VoucherLocalAccountantDelegate_accounts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
@Join
@Persistent(
	nullValue=NullValue.EXCEPTION,
	table="JFireVoucher_VoucherLocalAccountantDelegate_accounts",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, Account> accounts;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherLocalAccountantDelegate() { }

	public VoucherLocalAccountantDelegate(String organisationID, String localAccountantDelegateID)
	{
		super(organisationID, localAccountantDelegateID);
		accounts = new HashMap<String, Account>();
//		this.account = account;
//		if (!(ACCOUNT_ANCHOR_TYPE_ID_VOUCHER.equals(account.getAnchorTypeID())))
//			throw new IllegalArgumentException("account.anchorType is invalid! Must be ACCOUNT_ANCHOR_TYPE_ID_VOUCHER='"+ACCOUNT_ANCHOR_TYPE_ID_VOUCHER+"'!!!");
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.accounting.book.LocalAccountantDelegate#bookArticle(org.nightlabs.jfire.trade.OrganisationLegalEntity, org.nightlabs.jfire.security.User, org.nightlabs.jfire.accounting.Invoice, org.nightlabs.jfire.trade.Article, org.nightlabs.jfire.accounting.book.BookMoneyTransfer, java.util.Map)
	 */
	@Override
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article, BookMoneyTransfer container,
			Set<Anchor> involvedAnchors)
	{
		LinkedList<ArticlePrice> articlePriceStack = new LinkedList<ArticlePrice>();
		articlePriceStack.add(article.getPrice());
		bookProductTypeParts(mandator, user, articlePriceStack, 1, container, involvedAnchors);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<String, Account> unmodifiableAccounts = null;

	/**
	 * @return a Map with {@link Currency}'s id as
	 */
	public Map<String, Account> getAccounts()
	{
		if (unmodifiableAccounts == null) {
			derbyWorkaround();
			unmodifiableAccounts = Collections.unmodifiableMap(accounts);
		}

		return unmodifiableAccounts;
	}

	public void setAccount(String currencyID, Account account)
	{
		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		if (account == null)
			accounts.remove(currencyID);
		else {
			if (!currencyID.equals(account.getCurrency().getCurrencyID()))
				throw new IllegalArgumentException("The given currencyID '" + currencyID + "' does not match the currency '" + account.getCurrency().getCurrencyID() + "' of the given account '" + account.getPrimaryKey() + "'!");

			accounts.put(currencyID, account);
		}
	}

	private final void derbyWorkaround()
	{
		// TODO this method is a WORKAROUND for this issue: http://www.datanucleus.org/servlet/jira/browse/NUCRDBMS-379
		// Derby until (including) 10.5.3.0 has this bug (and maybe even newer versions). It is *not* a DataNucleus bug.
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			return;

		FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
		try {
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			pm.refresh(this);
			this.accounts.values();
		} finally {
			NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
		}
	}

	@Override
	public void bookProductTypeParts(OrganisationLegalEntity mandator, User user, LinkedList<ArticlePrice> articlePriceStack, int delegationLevel, BookMoneyTransfer container, Set<Anchor> involvedAnchors) {
		ArticlePrice articlePrice = articlePriceStack.peek();
		PersistenceManager pm = getPersistenceManager();
		String currencyID = articlePrice.getCurrency().getCurrencyID();
		derbyWorkaround();
		Account account = accounts.get(currencyID);
		if (account == null) // TODO maybe this should be a different exception in order to react on it specifically
			throw new IllegalStateException("The VoucherLocalAccountantDelegate does not contain an account for currencyID '"+currencyID+"'!!! name='"+getName().getText()+"' id='"+JDOHelper.getObjectId(this)+"'");

		Anchor from = mandator;
		Anchor to = account;
		long amount = articlePrice.getAmount();

		if (amount < 0) {
			Anchor tmp = from;
			from = to;
			to = tmp;
		}

		Invoice invoice = container.getInvoice();
		if (invoice.getCustomer().equals(mandator)) {
			// if the local organisation is the customer of the invoice
			// we revert the transfer direction of ALL resolved transfers!
			Anchor tmp = from;
			from = to;
			to = tmp;
		}

		amount = Math.abs(amount);

		VoucherMoneyTransfer moneyTransfer = new VoucherMoneyTransfer(
				InvoiceMoneyTransfer.BOOK_TYPE_BOOK,
				container,
				from, to,
				container.getInvoice(),
				amount,
				articlePrice.getArticle());
		moneyTransfer = pm.makePersistent(moneyTransfer);
		moneyTransfer.bookTransfer(user, involvedAnchors);
	}
}
