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
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * An instance of {@link VoucherLocalAccountantDelegate} is assigned to {@link ProductType}s of type
 * {@link VoucherType}. It directs money from/to an {@link Account} defined for
 * the delegate. Accounts for this delegate are defined per {@link Currency} (for each currency,
 * there's either zero or one account).
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherLocalAccountantDelegate"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=VoucherLocalAccountantDelegate.FETCH_GROUP_VOUCHER_LOCAL_ACCOUNTS,
		members=@Persistent(name="accounts")
	)
})
public class VoucherLocalAccountantDelegate
extends LocalAccountantDelegate
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_VOUCHER_LOCAL_ACCOUNTS = "VoucherLocalAccountantDelegate.accounts";

	@Join
	@Persistent(
			nullValue=NullValue.EXCEPTION,
			table="JFireVoucher_VoucherLocalAccountantDelegate_accounts"
	)
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
	}

	@Override
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article, BookMoneyTransfer container,
			Set<Anchor> involvedAnchors)
	{
		LinkedList<ArticlePrice> articlePriceStack = new LinkedList<ArticlePrice>();
		articlePriceStack.add(article.getPrice());
		bookProductTypeParts(mandator, user, articlePriceStack, 1, container, involvedAnchors);
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<String, Account> unmodifiableAccounts = null;

	/**
	 * Get the accounts that are used for money transfers.
	 * <p>
	 * The returned <code>Map</code> is unmodifiable - to change the mapping from currency to account,
	 * use the {@link #setAccount(String, Account)} method.
	 * </p>
	 *
	 * @return a read-only <code>Map</code> with {@link Currency}'s ID (e.g. "EUR") as key and {@link Account} as value.
	 * @see #setAccount(String, Account)
	 */
	public Map<String, Account> getAccounts()
	{
		if (unmodifiableAccounts == null) {
			derbyWorkaround();
			unmodifiableAccounts = Collections.unmodifiableMap(accounts);
		}

		return unmodifiableAccounts;
	}

	/**
	 * Assign an {@link Account} for the money transfers in a certain currency. For every
	 * currency, there is exactly zero or one <code>Account</code>. Vouchers can only be sold
	 * or redeemed in a certain currency, if there's an account assigned.
	 * <p>
	 * To remove an assignment, you can pass <code>account = null</code>.
	 * </p>
	 *
	 * @param currencyID the ID of the {@link Currency}. This argument must <b>not</b> be <code>null</code>.
	 * @param account the account to assign to the currency - or <code>null</code> to clear a previous assignment.
	 * If this argument is not <code>null</code>, the {@link Account#getCurrency()} must match the <code>currencyID</code>.
	 */
	public void setAccount(String currencyID, Account account)
	{
		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		derbyWorkaround();
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

		// Even though the unmodifiable Map should be backed, we better null it here. Maybe the refresh
		// causes a new instance to be assigned to this.accounts.
		unmodifiableAccounts = null;
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
			// we inverse the transfer direction of ALL resolved transfers!
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
