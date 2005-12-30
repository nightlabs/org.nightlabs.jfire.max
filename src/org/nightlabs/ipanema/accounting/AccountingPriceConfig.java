/*
 * Created on Feb 26, 2005
 */
package org.nightlabs.ipanema.accounting;

import java.util.LinkedList;

import org.nightlabs.ipanema.accounting.priceconfig.PriceConfig;
import org.nightlabs.ipanema.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.ipanema.store.NestedProductType;
import org.nightlabs.ipanema.store.Product;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.trade.ArticlePrice;

/**
 * This is a pseudo <tt>PriceConfig</tt> which manages <tt>Price</tt> instances
 * whenever a <tt>Price</tt> is needed outside of a real <tt>PriceConfig</tt>
 * (for example in <tt>Offer</tt>s).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *		table="JFireTrade_AccountingPriceConfig"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class AccountingPriceConfig extends PriceConfig
{

	protected AccountingPriceConfig()
	{
	}

	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public AccountingPriceConfig(String organisationID, long priceConfigID)
	{
		super(organisationID, priceConfigID);
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.PriceConfig#isDependentOnOffer()
	 */
	public boolean isDependentOnOffer()
	{
		return false;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.PriceConfig#requiresProductTypePackageInternal()
	 */
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.PriceConfig#createArticlePrice(Article)
	 */
	public ArticlePrice createArticlePrice(Article article)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.ipanema.trade.Article, java.util.LinkedList, org.nightlabs.ipanema.trade.ArticlePrice, org.nightlabs.ipanema.trade.ArticlePrice, java.util.LinkedList, org.nightlabs.ipanema.store.NestedProductType, java.util.LinkedList)
	 */
	public ArticlePrice createNestedArticlePrice(IPackagePriceConfig packagePriceConfig, Article article, LinkedList priceConfigStack, ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice, LinkedList articlePriceStack, NestedProductType nestedProductType, LinkedList nestedProductTypeStack)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.ipanema.trade.Article, java.util.LinkedList, org.nightlabs.ipanema.trade.ArticlePrice, org.nightlabs.ipanema.trade.ArticlePrice, java.util.LinkedList, NestedProductType, LinkedList, org.nightlabs.ipanema.store.Product, java.util.LinkedList)
	 */
	public ArticlePrice createNestedArticlePrice(IPackagePriceConfig packagePriceConfig, Article article, LinkedList priceConfigStack, ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice, LinkedList articlePriceStack, NestedProductType nestedProductType, LinkedList nestedProductTypeStack, Product nestedProduct, LinkedList productStack)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

}
