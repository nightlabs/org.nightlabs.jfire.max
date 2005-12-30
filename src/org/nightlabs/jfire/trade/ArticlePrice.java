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

package org.nightlabs.jfire.trade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.AccountingPriceConfig;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;


/**
 * This ArticlePrice is used within OfferItemS. On creation, it copies all data from the original
 * price. Thus, the price grid can be changed without the offers to change. The offer items
 * call ArticlePrice.assign(...) only if the offer changes.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.Price"
 *		detachable="true"
 *		table="JFireTrade_ArticlePrice"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ArticlePrice.packageArticlePrice" fields="packageArticlePrice"
 * @jdo.fetch-group name="ArticlePrice.nestedArticlePrices" fields="nestedArticlePrices"
 * @jdo.fetch-group name="ArticlePrice.origPrice" fields="origPrice"
 * @jdo.fetch-group name="ArticlePrice.this" fetch-groups="default" fields="origPrice, nestedArticlePrices, packageArticlePrice"
 */
public class ArticlePrice extends org.nightlabs.jfire.accounting.Price
{
	public static final String FETCH_GROUP_PACKAGE_ARTICLE_PRICE = "ArticlePrice.packageArticlePrice";
	public static final String FETCH_GROUP_NESTED_ARTICLE_PRICES = "ArticlePrice.nestedArticlePrices";
	public static final String FETCH_GROUP_ORIG_PRICE = "ArticlePrice.origPrice";
	public static final String FETCH_GROUP_THIS_ARTICLE_PRICE = "ArticlePrice.this";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private org.nightlabs.jfire.accounting.Price origPrice;

	/**
	 * key: String productTypePK<br/>
	 * value: ArticlePrice offerItemPrice
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ArticlePrice"
	 *		dependent="true"
	 *		mapped-by="packageArticlePrice"
	 *
	 * @jdo.key mapped-by="nestKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="nestKey"
	 */
	protected Map nestedArticlePrices = new HashMap();

	/**
	 * This is to map entries in nestedArticlePrices.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ArticlePrice packageArticlePrice;

	/**
	 * The article for which this <tt>ArticlePrice</tt> has been created. Note, that
	 * <tt>Article.articlePrice</tt> will point to another <tt>ArticlePrice</tt>, if
	 * this <tt>ArticlePrice</tt> is nested within another. <tt>Article.articlePrice</tt>
	 * points always to the toplevel package<tt>ArticlePrice</tt> (the root).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Article article;

	/**
	 * This is to map entries in nestedArticlePrices. It is either the <tt>productTypePK</tt>
	 * or the <tt>productPK</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String nestKey;

	/**
	 * If this <tt>ArticlePrice</tt> is nested, then this points to the
	 * <tt>NestedProductType</tt> for which this price has been calculated and assigned.
	 * This <tt>ArticlePrice</tt> represents always the TOTAL price, means it might
	 * differ from the <tt>origPrice</tt>, if the quantity of <tt>ProductType</tt> within
	 * the <tt>nestedProductType</tt> is not 1. 
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private NestedProductType nestedProductType = null;

	/**
	 * This points always to the <tt>ProductType</tt> for which this price has been
	 * created.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;

	/**
	 * This is <tt>null</tt>, if the offer is in noncommittal and no products have been
	 * assigned (at least for the corresponding <tt>Article</tt>). Otherwise, it points to
	 * the <tt>Product</tt> for which this price has been created.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Product product = null;

	/**
	 *  @jdo.field persistence-modifier="persistent"
	 */
	private boolean virtualInner;

	protected ArticlePrice() { }

	/**
	 * This constructor creates a nested <tt>ArticlePrice</tt>. Hence it is necessary
	 * to pass the direct parent <tt>packageArticlePrice</tt>. The new instance will
	 * automatically be registered in the given <tt>packageArticlePrice</tt>.
	 *
	 * @param article The <tt>Article</tt> for which this <tt>ArticlePrice</tt> is
	 *		created. Note: <tt>Article.articlePrice</tt> points always to the root <tt>ArticlePrice</tt>
	 *		and might therefore point to another instance of <tt>ArticlePrice</tt>.
	 * @param origPrice The original price from which to copy all data. If
	 *		<tt>origPrice.priceID >= 0</tt>, the origPrice will be stored as reference
	 *		within the new <tt>ArticlePrice</tt>.
	 * @param organisationID The organisationID of the <tt>IPriceConfig</tt> which
	 *		provides the <tt>priceID</tt>. This must be equal to <tt>Accounting.organisationID</tt>,
	 *		because <tt>Article</tt>s cannot be created in a foreign datastore.
	 * @param priceConfigID Together with the <tt>organisationID</tt> part of the primary key
	 *		of the responsible <tt>IPriceConfig</tt>.
	 * @param priceID The ID for the new price within the scope of <tt>organisationID</tt> and
	 *		<tt>priceConfigID</tt>.
	 * @param packageArticlePrice Either <code>null</code> or the parent ArticlePrice into which this
	 *		new ArticlePrice will be nested.
	 * @param nestedProductType Either <code>null</code> or the NestedProductType for which's inner part
	 *		this ArticlePrice is created.
	 * @param productType The ProductType for which this ArticlePrice is created.
	 * @param product The Product for which this ArticlePrice is created.
	 * @param refund If <tt>true</tt>, the price will be negated.
	 */
	public ArticlePrice(
			Article article,
			org.nightlabs.jfire.accounting.Price origPrice,
			String organisationID, long priceConfigID,
			long priceID,
			ArticlePrice packageArticlePrice,
			NestedProductType nestedProductType,
			ProductType productType,
			Product product,
			boolean virtualInner,
			boolean refund)
	{
		super(organisationID, priceConfigID, priceID, origPrice.getCurrency());
		if (article == null)
			throw new NullPointerException("article");

		if (origPrice == null)
			throw new NullPointerException("origPrice");

		if (productType == null)
			throw new NullPointerException("productType");

		this.article = article;
		this.packageArticlePrice = packageArticlePrice;

		if (nestedProductType != null && !nestedProductType.getInnerProductTypePrimaryKey().equals(nestedProductType.getPackageProductType().getPrimaryKey()))
			this.nestedProductType = nestedProductType;

		this.product = product;
		this.productType = productType;

		if (product != null)
			nestKey = product.getPrimaryKey();
		else
			nestKey = productType.getPrimaryKey();

		if (origPrice.getPriceID() >= 0)
			this.origPrice = origPrice;
		
		this.virtualInner = virtualInner;

		assign(origPrice, refund);

		if (packageArticlePrice != null)
			packageArticlePrice.addNestedArticlePrice(this);
	}

	/**
	 * This method is called by the constructor.
	 */
	protected void addNestedArticlePrice(ArticlePrice nestedArticlePrice)
	{
		if (!getPrimaryKey().equals(nestedArticlePrice.getPackageArticlePrice().getPrimaryKey()))
			throw new IllegalArgumentException("nestedArticlePrice.packageArticlePrice != this!!!");

		nestedArticlePrices.put(nestedArticlePrice.getNestKey(), nestedArticlePrice);
	}

	/**
	 * This constructor creates a top level <tt>ArticlePrice</tt>.
	 *
	 * @param article The <tt>Article</tt> for which this <tt>ArticlePrice</tt> has been
	 *		created. Note: <tt>Article.articlePrice</tt> points always to the root <tt>ArticlePrice</tt>
	 *		and might therefore point to another instance of <tt>ArticlePrice</tt>.
	 * @param origPrice The original price from which to copy all data. If
	 *		<tt>origPrice.priceID >= 0</tt>, the origPrice will be stored as reference
	 *		within the new <tt>ArticlePrice</tt>. 
	 *  @param organisationID The organisationID of the <tt>IPriceConfig</tt> which
	 *		provides the <tt>priceID</tt>. This must be equal to <tt>Accounting.organisationID</tt>,
	 *		because <tt>Article</tt>s cannot be created in a foreign datastore.
	 * @param priceConfigID Together with the <tt>organisationID</tt> part of the primary key
	 *		of the responsible <tt>IPriceConfig</tt>.
	 * @param packageArticlePrice
	 * @param priceID The ID for the new price within the scope of <tt>organisationID</tt> and
	 *		<tt>priceConfigID</tt>.
	 * @param refund If <tt>true</tt>, the price will be negated.
	 */
	public ArticlePrice(
			Article article,
			org.nightlabs.jfire.accounting.Price origPrice,
			String organisationID, long priceConfigID,
			long priceID, boolean refund)
	{
		this(article, origPrice, organisationID, priceConfigID, priceID,
				null, null, article.getProductType(), article.getProduct(), false, refund);		
	}

	/**
	 * Util method to be used by PriceConfig implmentations when
	 * generating OfferItemPrices.
	 * 
	 * Copies all data (amount, fragments) from the given origPrice.
	 *
	 * @param origPrice
	 */
	protected void assign(org.nightlabs.jfire.accounting.Price origPrice, boolean refund)
	{
		if (!getCurrency().getCurrencyID().equals(origPrice.getCurrency().getCurrencyID()))
			throw new IllegalArgumentException("Currencies do not match!");

		this.setAmount(origPrice.getAmount());

		for (Iterator it = origPrice.getFragments().iterator(); it.hasNext(); ) {
			PriceFragment origpf = (PriceFragment)it.next();
			PriceFragment pf = new PriceFragment(this, origpf);
			// TODO Does the following "put" really delete an old object?
			// Right now, it's not so important, because we currently use assign only for
			// creation, but this will change once we allow price changes to update article
			// prices.
			addPriceFragment(pf);
		}
		
		if (refund)
			negate();

		if (origPrice instanceof ArticlePrice) {
			ArticlePrice origArticlePrice = (ArticlePrice)origPrice;
			PersistenceManager pm = JDOHelper.getPersistenceManager(origArticlePrice);
			AccountingPriceConfig accountingPriceConfig = Accounting.getAccounting(pm).getAccountingPriceConfig();

			for (Iterator it = origArticlePrice.getNestedArticlePrices().iterator(); it.hasNext(); ) {
				ArticlePrice origNestedArticlePrice = (ArticlePrice) it.next();

				new ArticlePrice(
						origArticlePrice.article,
						origNestedArticlePrice,
						accountingPriceConfig.getOrganisationID(),
						accountingPriceConfig.getPriceConfigID(),
						accountingPriceConfig.createPriceID(),
						this,
						origNestedArticlePrice.nestedProductType,
						origNestedArticlePrice.productType,
						origNestedArticlePrice.product,
						origNestedArticlePrice.virtualInner,
						refund);
			}
		}
	}

	/**
	 * Negates the amounts of all <tt>PriceFragment</tt>s. It does not dive into
	 * packaged <tt>ArticlePrice</tt>s! 
	 */
	protected void negate()
	{
		this.setAmount(-this.getAmount());
		for (Iterator it = this.getFragments().iterator(); it.hasNext(); ) {
			PriceFragment pf = (PriceFragment)it.next();
			pf.setAmount(-pf.getAmount());
		}
	}

	/**
	 * This method creates a new instance of ArticlePrice in which
	 * the original price represented by this instance of ArticlePrice
	 * is negated.
	 * 
	 * @param priceConfig The priceConfig is used to create an ID for the
	 * new ArticlePrice
	 *
	 * @return
	 */
	public ArticlePrice createRefundPrice()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ArticlePrice is currently not persistent!");

		Accounting accounting = Accounting.getAccounting(pm);
		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

		ArticlePrice articlePrice = new ArticlePrice(
				this.article,
				this,
				accounting.getOrganisationID(),
				accountingPriceConfig.getPriceConfigID(),
				accountingPriceConfig.createPriceID(), 
				true
			);
		return articlePrice;
	}

	public Collection getNestedArticlePrices() {
		return nestedArticlePrices.values();
	}

	/**
	 * Returns the packaged ArticlePrice for the given product.
	 *
	 * @param product
	 * @param throwException
	 * @return The packaged ArticlePrice for the given product.
	 * @throws ArticlePriceNotFoundException
	 */
	public ArticlePrice getNestedArticlePrice(Product product, boolean throwException) 
	{
		ArticlePrice packagedPrice = (ArticlePrice)nestedArticlePrices.get(product.getPrimaryKey());
		if ((packagedPrice == null) && (throwException))
			throw new ArticlePriceNotFoundException("Could not find a packaged ArticlePrice for product "+product.getPrimaryKey());
		return packagedPrice;
	}

	/**
	 * Returns the packaged ArticlePrice for the given productType.
	 * 
	 * @param productType
	 * @param throwException
	 * @return The packaged ArticlePrice for the given productType.
	 * @throws ArticlePriceNotFoundException
	 */
	public ArticlePrice getNestedArticlePrice(ProductType productType, boolean throwException) 
	{
		ArticlePrice packagedPrice = (ArticlePrice)nestedArticlePrices.get(productType.getPrimaryKey());
		if ((packagedPrice == null) && (throwException))
			throw new ArticlePriceNotFoundException("Could not find a packaged ArticlePrice for productType "+productType.getPrimaryKey());
		return packagedPrice;
	}

//	/**
//	 * @param productType
//	 * @return The packaged ArticlePrice for the given productType.
//	 * @throws ArticlePriceNotFoundException
//	 */
//	public ArticlePrice getNestedArticlePrice(ProductType productType) 
//	{
//		return getNestedArticlePrice(productType, true);
//	}

	public ArticlePrice getPackageArticlePrice() {
		return packageArticlePrice;
	}

	public String getNestKey() {
		return nestKey;
	}

	/**
	 * @return Returns the article.
	 */
	public Article getArticle()
	{
		return article;
	}
	/**
	 * @return Returns the nestedProductType.
	 */
	public NestedProductType getNestedProductType()
	{
		return nestedProductType;
	}
	/**
	 * @return Returns the origPrice.
	 */
	public org.nightlabs.jfire.accounting.Price getOrigPrice()
	{
		return origPrice;
	}
	/**
	 * @return Returns the product.
	 */
	public Product getProduct()
	{
		return product;
	}
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}
	/**
	 * @return Returns the virtualInner.
	 */
	public boolean isVirtualInner()
	{
		return virtualInner;
	}
}
