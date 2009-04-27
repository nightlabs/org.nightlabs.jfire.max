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

import java.util.LinkedList;

import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * This is a pseudo <tt>PriceConfig</tt> which manages <tt>Price</tt> instances
 * whenever a <tt>Price</tt> is needed outside of a real <tt>PriceConfig</tt>
 * (for example in <tt>Offer</tt>s).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *		table="JFireTrade_AccountingPriceConfig"
 *
 * @jdo.inheritance strategy = "new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_AccountingPriceConfig")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class AccountingPriceConfig extends PriceConfig
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AccountingPriceConfig() { }

	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public AccountingPriceConfig(String organisationID, String priceConfigID)
	{
		super(organisationID, priceConfigID);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.PriceConfig#isDependentOnOffer()
	 */
	@Override
	public boolean isDependentOnOffer()
	{
		return false;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.PriceConfig#requiresProductTypePackageInternal()
	 */
	@Override
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.PriceConfig#createArticlePrice(Article)
	 */
	@Override
	public ArticlePrice createArticlePrice(Article article)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.jfire.trade.Article, LinkedList, org.nightlabs.jfire.trade.ArticlePrice, org.nightlabs.jfire.trade.ArticlePrice, LinkedList, org.nightlabs.jfire.store.NestedProductTypeLocal, LinkedList)
	 */
	public ArticlePrice createNestedArticlePrice(IPackagePriceConfig packagePriceConfig, Article article, LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack, NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.jfire.trade.Article, LinkedList, org.nightlabs.jfire.trade.ArticlePrice, org.nightlabs.jfire.trade.ArticlePrice, LinkedList, NestedProductTypeLocal, LinkedList, org.nightlabs.jfire.store.Product, LinkedList)
	 */
	public ArticlePrice createNestedArticlePrice(IPackagePriceConfig packagePriceConfig, Article article, LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack, NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack, Product nestedProduct, LinkedList<Product> productStack)
	{
		throw new UnsupportedOperationException("This pseudo PriceConfig is not intended to be used in this context!");
	}

}
