package org.nightlabs.jfire.voucher.accounting;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
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
 * @!jdo.query
 *		name="getVoucherLocalAccountantDelegateByAccount"
 *		query="SELECT UNIQUE WHERE this.account == :account"
 */
public class VoucherLocalAccountantDelegate
extends LocalAccountantDelegate
{
	private static final long serialVersionUID = 1L;

//	public static final String ACCOUNT_ANCHOR_TYPE_ID_VOUCHER = "Account.Voucher";

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
	@Implement
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
	private transient Map<String, Account> unmodifiableAccounts = null;

	/**
	 * @return a Map with {@link Currency}'s id as 
	 */
	public Map<String, Account> getAccounts()
	{
		if (unmodifiableAccounts == null)
			unmodifiableAccounts = Collections.unmodifiableMap(accounts);

		return unmodifiableAccounts;
	}

	public void setAccount(String currencyID, Account account)
	{
		if (account == null)
			accounts.remove(currencyID);
		else
			accounts.put(currencyID, account);
	}

	@Override
	public void bookProductTypeParts(OrganisationLegalEntity mandator, User user, LinkedList<ArticlePrice> articlePriceStack, int delegationLevel, BookMoneyTransfer container, Set<Anchor> involvedAnchors) {
		ArticlePrice articlePrice = articlePriceStack.peek();
		PersistenceManager pm = getPersistenceManager();
		String currencyID = articlePrice.getCurrency().getCurrencyID();
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
