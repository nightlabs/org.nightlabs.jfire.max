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

package org.nightlabs.jfire.accounting.priceconfig;

import java.util.Collection;
import java.util.LinkedList;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IPriceConfig
{
	/**
	 * @return Returns the organisationID.
	 */
	String getOrganisationID();

	/**
	 * @return Returns the priceConfigID.
	 */
	long getPriceConfigID();

	String getPrimaryKey();

	Collection getCurrencies();

	/**
	 * @param currency The Currency to add.
	 *
	 * @see #beginAdjustParameters()
	 * @see #endAdjustParameters()
	 */
	void addCurrency(Currency currency);

	/**
	 * @return Returns the desired Currency if registered or <tt>null</tt> if the
	 * given currencyID is not known.
	 */
	Currency getCurrency(String currencyID,
			boolean throwExceptionIfNotRegistered);

	boolean containsCurrency(String currencyID);

	boolean containsCurrency(Currency currency);

	Currency removeCurrency(String currencyID);

	Collection getPriceFragmentTypes();

	void addPriceFragmentType(PriceFragmentType priceFragmentType);

	PriceFragmentType getPriceFragmentType(String organisationID,
			String priceFragmentTypeID, boolean throwExceptionIfNotExistent);

	PriceFragmentType getPriceFragmentType(String priceFragmentTypePK,
			boolean throwExceptionIfNotExistent);

	boolean containsPriceFragmentType(PriceFragmentType priceFragmentType);

	boolean containsPriceFragmentType(String priceFragmentTypePK);

	boolean containsPriceFragmentType(String organisationID,
			String priceFragmentTypeID);

	/**
	 * This method calls removePriceFragmentType(String priceFragmentTypePK), hence
	 * you don't need to overwrite this method to react on a remove.
	 *
	 * @see #removePriceFragmentType(String)
	 * @see PriceFragmentType#getPrimaryKey(String, String)
	 */
	PriceFragmentType removePriceFragmentType(String organisationID,
			String priceFragmentTypeID);

	/**
	 * @param priceFragmentTypePK The composite primary key of the PriceFragmentType to remove. 
	 * @return Returns the PriceFragmentType that has been removed or <tt>null</tt> if none was registered with the given key.
	 * 
	 * @see #removePriceFragmentType(String, String)
	 */
	PriceFragmentType removePriceFragmentType(String priceFragmentTypePK);

	/**
	 * Creates a <tt>priceID</tt> by incrementing the member nextPriceID.
	 * The new ID is unique within the context of this <tt>PriceConfig</tt>
	 * (<tt>organisationID</tt> & <tt>priceConfigID</tt>).
	 *
	 * @return Returns a price id, which is unique within the context of this <tt>PriceConfig</tt>.
	 */
	long createPriceID();

	/**
	 * @return Returns the name.
	 */
	public PriceConfigName getName();

	/**
	 * This method must create a new instance of <tt>ArticlePrice</tt> which itself
	 * contains already all nested <tt>ArticlePrice</tt>s for nested
	 * {@link org.nightlabs.jfire.store.ProductType}s.
	 * <p>
	 * This work is usually done by delegating to {@link PriceConfigUtil}.
	 * </p>
	 *
	 * @param topLevelPriceConfig This <tt>IPriceConfig</tt> is responsible for creating
	 *		a unique ID for the new <tt>ArticlePrice</tt>.
	 * @param article The <tt>Article</tt> for which the new <tt>ArticlePrice</tt> is
	 *		created. Note, that this method does not provide the top-level <tt>ArticlePrice</tt>,
	 *		but one for a nested level.
	 * @param priceConfigStack A <tt>List</tt> with all <tt>IPriceConfig</tt>s
	 *		which take part at forming the main <tt>ArticlePrice</tt> in the current nesting
	 *		hierarchy. This <tt>IPriceConfig</tt> must add itself at the first position (#0)
	 *		and remove itself before leaving this method. The top-level <tt>IPackagePriceConfig</tt>
	 *		is NOT contained by this <tt>List</tt>!
	 * @param topLevelArticlePrice The <tt>ArticlePrice</tt> of the
	 *		<tt>article</tt> is passed, because it is not yet assigned to Article, when this
	 *		method is called.
	 * @param nextLevelArticlePrice This <tt>ArticlePrice</tt> is the direct parent for
	 *		the <tt>ArticlePrice</tt> created by this method and needs to be passed to the
	 *		constructor of <tt>ArticlePrice</tt>.
	 * @param articlePriceStack A <tt>List</tt> containing all <tt>ArticlePrice</tt>s
	 *		of the nesting operation. It does NOT contain the <tt>topLevelArticlePrice</tt>!
	 *		This method must add the newly created <tt>ArticlePrice</tt> at the first position
	 *		(#0) before diving into
	 *		<tt>NestedProductType</tt>s. Before returning, this <tt>ArticlePrice</tt> must be
	 *		removed again.
	 * @param nestedProductType The <tt>NestedProductType</tt> for which to create a nested
	 *		<tt>ArticlePrice</tt>.
	 * @param nestedProductTypeStack A <tt>List</tt> containing all <tt>NestedProductType</tt>s
	 *		of the above nesting operation; hence not containing the top-level <tt>ProductType</tt>
	 *		which can be obtained by <tt>article.getProductType()</tt>. This method must add
	 *		the current <tt>nestedProductType</tt> as first element (position #0) before iterating
	 *		deeper and must remove it before leaving this method.
	 *
	 * @return Returns a new instance of <tt>ArticlePrice</tt>
	 */
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig,
			Article article,
			LinkedList priceConfigStack,
			ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice,
			LinkedList articlePriceStack,
			NestedProductType nestedProductType,
			LinkedList nestedProductTypeStack);

	/**
	 * Like
	 * {@link #createNestedArticlePrice(IPackagePriceConfig, Article, LinkedList, ArticlePrice, ArticlePrice, LinkedList, NestedProductType, LinkedList)
	 * this method creates a new instance of <tt>ArticlePrice</tt> for an <tt>Article</tt>.
	 * The difference however is, that this method is used for actual <tt>Product</tt>s and
	 * not for <tt>ProductType</tt>s.
	 * <p>
	 * You should delegate to
	 * {@link PriceConfigUtil#createNestedArticlePrice(IPackagePriceConfig, IPriceConfig, Article, LinkedList, ArticlePrice, ArticlePrice, LinkedList, NestedProductType, LinkedList, Product, LinkedList, Price)}!
	 * </p>
	 */
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig,
			Article article,
			LinkedList priceConfigStack,
			ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice,
			LinkedList articlePriceStack,
			NestedProductType nestedProductType,
			LinkedList nestedProductTypeStack,
			Product nestedProduct,
			LinkedList productStack);

}
