package org.nightlabs.jfire.voucher.accounting;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Invoice;
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

public class VoucherLocalAccountantDelegate
extends LocalAccountantDelegate
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherLocalAccountantDelegate() { }

	public VoucherLocalAccountantDelegate(String organisationID,
			String localAccountantDelegateID)
	{
		super(organisationID, localAccountantDelegateID);
	}

	public VoucherLocalAccountantDelegate(LocalAccountantDelegate parent,
			String organisationID, String localAccountantDelegateID)
	{
		super(parent, organisationID, localAccountantDelegateID);
	}

	@Implement
	public void bookArticle(OrganisationLegalEntity mandator, User user,
			Invoice invoice, Article article, BookMoneyTransfer container,
			Map<String, Anchor> involvedAnchors)
	{
		// TODO Auto-generated method stub

	}

	@Implement
	public Collection<BookInvoiceTransfer> getBookInvoiceTransfersForDimensionValues(
			OrganisationLegalEntity mandator,
			LinkedList<ArticlePrice> articlePriceStack,
			Map<String, String> dimensionValues, MoneyFlowMapping resolvedMapping,
			Map<ResolvedMapKey, ResolvedMapEntry> resolvedMappings,
			BookMoneyTransfer bookMoneyTransfer)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	public List<String> getMoneyFlowDimensionIDs()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	public String getMoneyFlowMappingKey(ProductTypeID productTypeID,
			String packageType, Map<String, String> dimensionValues, String currencyID)
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

	}

}
