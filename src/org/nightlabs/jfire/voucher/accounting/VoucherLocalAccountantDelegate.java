package org.nightlabs.jfire.voucher.accounting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.book.BookMoneyTransfer;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.MoneyFlowMapping;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
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

	public static final String ACCOUNT_ANCHOR_TYPE_ID_VOUCHER = "Account.Voucher";

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="Account"
	 *		table="JFireVoucher_VoucherLocalAccountantDelegate_accounts"
	 *
	 * @jdo.join
	 */
	private Map<String, Account> accounts;

//	/**
//	 * This method finds the <code>VoucherLocalAccountantDelegate</code> which references the {@link Account}
//	 * specified by <code>accountID</code>.
//	 *
//	 * @param pm The <code>PersistenceManager</code> used for accessing the datastore.
//	 * @param accountID The id of the account referenced by the delegates.
//	 * @return an <code>VoucherLocalAccountantDelegate</code> or <code>null</code>
//	 */
//	public static VoucherLocalAccountantDelegate getVoucherLocalAccountantDelegate(
//		PersistenceManager pm, AnchorID accountID)
//	{
//		pm.getExtent(Account.class);
//		Account account;
//		try {
//			account = (Account) pm.getObjectById(accountID);
//			account.getBalance(); // TODO remove this once it's sure that the JPOX bug doesn't exist anymore
//		} catch (JDOObjectNotFoundException x) {
//			return null;
//		}
//
//		Query q = pm.newNamedQuery(VoucherLocalAccountantDelegate.class, "getVoucherLocalAccountantDelegateByAccount");
//		return (VoucherLocalAccountantDelegate) q.execute(account);
//	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherLocalAccountantDelegate() { }

	public VoucherLocalAccountantDelegate(String organisationID, String localAccountantDelegateID)
	{
		super(organisationID, localAccountantDelegateID);
		accounts = new HashMap<String, Account>();
//		this.account = account;
//		if (!(ACCOUNT_ANCHOR_TYPE_ID_VOUCHER.equals(account.getAnchorTypeID())))
//			throw new IllegalArgumentException("account.anchorType is invalid! Must be ACCOUNT_ANCHOR_TYPE_ID_VOUCHER='"+ACCOUNT_ANCHOR_TYPE_ID_VOUCHER+"'!!!");
	}

	@Implement
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article, BookMoneyTransfer container,
			Map<String, Anchor> involvedAnchors)
	{
		Accounting accounting = Accounting.getAccounting(getPersistenceManager());
		String currencyID = article.getPrice().getCurrency().getCurrencyID();
		Account account = accounts.get(currencyID);
		if (account == null) // TODO maybe this should be a different exception in order to react on it specifically
			throw new IllegalStateException("The VoucherLocalAccountantDelegate does not contain an account for currencyID '"+currencyID+"'!!! name='"+getName().getText()+"' id='"+JDOHelper.getObjectId(this)+"'");

		Anchor from = mandator;
		Anchor to = account;
		long amount = article.getPrice().getAmount();

		if (amount < 0) {
			Anchor tmp = from;
			from = to;
			to = tmp;
		}

		amount = Math.abs(amount);

		InvoiceMoneyTransfer imt = new InvoiceMoneyTransfer(
				InvoiceMoneyTransfer.BOOK_TYPE_BOOK,
				accounting,
				container,
				from, to,
				invoice,
				amount);
		imt.bookTransfer(user, involvedAnchors);
	}

	@Implement
	public Collection<BookInvoiceTransfer> getBookInvoiceTransfersForDimensionValues(
			OrganisationLegalEntity mandator,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues, MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			BookMoneyTransfer bookMoneyTransfer)
	{
		throw new UnsupportedOperationException("This method should never be called in this implementation of LocalAccountantDelegate!");
	}

	@Implement
	public List<String> getMoneyFlowDimensionIDs()
	{
		throw new UnsupportedOperationException("This method should never be called in this implementation of LocalAccountantDelegate!");
	}

	@Implement
	public String getMoneyFlowMappingKey(ProductTypeID productTypeID,
			String packageType, Map<String, String> dimensionValues, String currencyID)
	{
		throw new UnsupportedOperationException("This method should never be called in this implementation of LocalAccountantDelegate!");
	}

	@Implement
	protected void internalBookProductTypeParts(
			OrganisationLegalEntity mandator,
			User user,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			LinkedList<ArticlePrice> articlePriceStack,
			ArticlePrice articlePrice,
			Map<Anchor, Map<Anchor, Collection<BookInvoiceTransfer>>> bookInvoiceTransfers,
			ProductType productType, String packageType, int delegationLevel,
			BookMoneyTransfer container, Map<String, Anchor> involvedAnchors)
	{
		throw new UnsupportedOperationException("This method should never be called in this implementation of LocalAccountantDelegate!");
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
}
