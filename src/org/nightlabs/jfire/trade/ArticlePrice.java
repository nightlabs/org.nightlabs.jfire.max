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
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.AccountingPriceConfig;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
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
 * @jdo.fetch-group name="ArticlePrice.nestedArticlePrices[-1]" fields="nestedArticlePrices[-1]"
 * @jdo.fetch-group name="ArticlePrice.origPrice" fields="origPrice"
 * @jdo.fetch-group name="ArticlePrice.productType" fields="productType"
 * @jdo.fetch-group name="ArticlePrice.packageProductType" fields="packageProductType"
 * @jdo.fetch-group name="ArticlePrice.product" fields="product"
 * @jdo.fetch-group name="ArticlePrice.article" fields="article"
 * @!jdo.fetch-group name="ArticlePrice.this" fetch-groups="default" fields="origPrice, nestedArticlePrices, packageArticlePrice, productType, packageProductType, product"
 *
 * @jdo.fetch-group name="Article.price" fields="article"
 *
 * @jdo.fetch-group
 * 		name="FetchGroupsTrade.articleCrossTradeReplication"
 * 		fields="packageArticlePrice, article, productType, product, packageProductType"
 */
public class ArticlePrice extends org.nightlabs.jfire.accounting.Price
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PACKAGE_ARTICLE_PRICE = "ArticlePrice.packageArticlePrice";
	public static final String FETCH_GROUP_NESTED_ARTICLE_PRICES = "ArticlePrice.nestedArticlePrices";
	public static final String FETCH_GROUP_NESTED_ARTICLE_PRICES_NO_LIMIT = "ArticlePrice.nestedArticlePrices[-1]";
	public static final String FETCH_GROUP_ORIG_PRICE = "ArticlePrice.origPrice";
	public static final String FETCH_GROUP_ARTICLE = "ArticlePrice.article";
//	/**
//	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
//	 */
//	@Deprecated
//	public static final String FETCH_GROUP_THIS_ARTICLE_PRICE = "ArticlePrice.this";

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
	 *		dependent-value="true"
	 *		mapped-by="packageArticlePrice"
	 *
	 * @jdo.key mapped-by="nestKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="nestKey"
	 */
	protected Map<String, ArticlePrice> nestedArticlePrices;

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

//	/**
//	 * If this <tt>ArticlePrice</tt> is nested, then this points to the
//	 * <tt>NestedProductTypeLocal</tt> for which this price has been calculated and assigned.
//	 * This <tt>ArticlePrice</tt> represents always the TOTAL price, means it might
//	 * differ from the <tt>origPrice</tt>, if the quantity of <tt>ProductType</tt> within
//	 * the <tt>nestedProductTypeLocal</tt> is not 1.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private NestedProductTypeLocal nestedProductTypeLocal = null;

	/**
	 * @see #getPackageProductType()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType packageProductType;

	/**
	 * @see #getInnerProductTypeQuantity()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int innerProductTypeQuantity = 0;

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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean virtualInner;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
	 * @param nestedProductTypeLocal Either <code>null</code> or the NestedProductTypeLocal for which's inner part
	 *		this ArticlePrice is created.
	 * @param productType The ProductType for which this ArticlePrice is created.
	 * @param product The Product for which this ArticlePrice is created.
	 * @param refund If <tt>true</tt>, the price will be negated.
	 */
	public ArticlePrice(
			Article article,
			org.nightlabs.jfire.accounting.Price origPrice,
			String organisationID, // String priceConfigID,
			long priceID,
			ArticlePrice packageArticlePrice,
			NestedProductTypeLocal nestedProductTypeLocal,
			ProductType productType,
			Product product,
			boolean virtualInner,
			boolean refund)
	{
		this(
				article,
				origPrice,
				organisationID, // priceConfigID,
				priceID,
				packageArticlePrice,
				(
						nestedProductTypeLocal == null ||
						nestedProductTypeLocal.getInnerProductTypePrimaryKey().equals(nestedProductTypeLocal.getPackageProductTypeLocal().getPrimaryKey()) ?
								null : nestedProductTypeLocal.getPackageProductTypeLocal().getProductType()
				),
				(
						nestedProductTypeLocal == null ||
						nestedProductTypeLocal.getInnerProductTypePrimaryKey().equals(nestedProductTypeLocal.getPackageProductTypeLocal().getPrimaryKey()) ?
								0 : nestedProductTypeLocal.getQuantity()
				),
				productType,
				product,
				virtualInner,
				refund
		);
	}
	public ArticlePrice(
			Article article,
			org.nightlabs.jfire.accounting.Price origPrice,
			String organisationID, // String priceConfigID,
			long priceID,
			ArticlePrice packageArticlePrice,
			ProductType packageProductType,
			int innerProductTypeQuantity,
			ProductType productType,
			Product product,
			boolean virtualInner,
			boolean refund)
	{
		super(organisationID, priceID, origPrice.getCurrency());
		if (article == null)
			throw new NullPointerException("article");

		if (origPrice == null)
			throw new NullPointerException("origPrice");

		if (productType == null)
			throw new NullPointerException("productType");

		this.article = article;
		this.packageArticlePrice = packageArticlePrice;

		this.packageProductType = packageProductType;
		this.innerProductTypeQuantity = innerProductTypeQuantity;

		this.product = product;
		this.productType = productType;

		if (product != null)
			nestKey = product.getPrimaryKey();
		else
			nestKey = productType.getPrimaryKey();

		if (origPrice.getPriceID() >= 0)
			this.origPrice = origPrice;

		this.virtualInner = virtualInner;

		this.nestedArticlePrices = new HashMap<String, ArticlePrice>();
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
			String organisationID, // String priceConfigID,
			long priceID, boolean refund)
	{
		this(
				article, origPrice, organisationID, // priceConfigID,
				priceID,
				null, null, article.getProductType(), article.getProduct(), false, refund
		);
	}

	/**
	 * This method is used internally by the constructor. It copies all data (amount, fragments) from the given origPrice
	 * and if <code>refund == true</code>, it inverses them.
	 *
	 * @param origPrice The original price to be copied.
	 * @param refund Whether the values must be inversed (i.e. multiplied with -1).
	 */
	protected void assign(org.nightlabs.jfire.accounting.Price origPrice, boolean refund)
	{
		if (!getCurrency().getCurrencyID().equals(origPrice.getCurrency().getCurrencyID()))
			throw new IllegalArgumentException("Currencies do not match!");

		for (PriceFragment origpf : origPrice.getFragments()) {
			PriceFragment pf = new PriceFragment(this, origpf);
			// TODO Does the following "put" really delete an old object?
			// Right now, it's not so important, because we currently use assign only for
			// creation, but this will change once we allow price changes to update article
			// prices.
			addPriceFragment(pf);
		}

		this.setAmount(origPrice.getAmount());

		if (refund)
			negate();

		if (origPrice instanceof ArticlePrice) {
			ArticlePrice origArticlePrice = (ArticlePrice)origPrice;
//			PersistenceManager pm = JDOHelper.getPersistenceManager(origArticlePrice);
//			AccountingPriceConfig accountingPriceConfig = Accounting.getAccounting(pm).getAccountingPriceConfig();

			for (ArticlePrice origNestedArticlePrice : origArticlePrice.getNestedArticlePrices()) {
				new ArticlePrice(
						origArticlePrice.article,
						origNestedArticlePrice,
						IDGenerator.getOrganisationID(),
						IDGenerator.nextID(Price.class),
//						accountingPriceConfig.getOrganisationID(),
//						accountingPriceConfig.getPriceConfigID(),
//						accountingPriceConfig.createPriceID(),
						this,
						origNestedArticlePrice.packageProductType,
						origNestedArticlePrice.innerProductTypeQuantity,
						origNestedArticlePrice.productType,
						origNestedArticlePrice.product,
						origNestedArticlePrice.virtualInner,
						refund);
			}
		}
	}

	/**
	 * Negates the amounts of all <tt>PriceFragment</tt>s. It does not dive into
	 * packaged <tt>ArticlePrice</tt>s! Method is used internally by
	 * {@link #assign(org.nightlabs.jfire.accounting.Price, boolean) }.
	 */
	protected void negate()
	{
		this._setAmount(-this.getAmount());
		for (PriceFragment pf : this.getFragments()) {
			pf.setAmount(-pf.getAmount());
		}
	}

	/**
	 * This method creates a new instance of ArticlePrice in which
	 * the original price represented by this instance of ArticlePrice
	 * is negated.
	 *
	 * @return a newly created instance of ArticlePrice which will
	 *		have the same but <b>negative</b> value of this original
	 *		price. The newly created ArticlePrice will point (via {@link ArticlePrice#getArticle()})
	 *		back to the reversing Article (which is obtained by this method via <code>this.article.getReversingArticle()</code>).
	 */
	public ArticlePrice createReversingArticlePrice()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ArticlePrice is currently not persistent!");

		if (!this.article.isReversed())
			throw new IllegalStateException("this.article is not reversed! Cannot create a reversing price!");

		Article reversingArticle = this.article.getReversingArticle();
		if (reversingArticle == null)
			throw new IllegalStateException("this.article.getReversingArticle() returned null, even though this.article.isReversed() returned true!");

//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

		ArticlePrice articlePrice = new ArticlePrice(
				reversingArticle,
				this,
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(Price.class),
//				accounting.getOrganisationID(),
//				accountingPriceConfig.getPriceConfigID(),
//				accountingPriceConfig.createPriceID(),
				true
			);
		return articlePrice;
	}

	public Collection<ArticlePrice> getNestedArticlePrices() {
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
		ArticlePrice packagedPrice = nestedArticlePrices.get(product.getPrimaryKey());
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
		ArticlePrice packagedPrice = nestedArticlePrices.get(productType.getPrimaryKey());
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
	 * Because {@link NestedProductTypeLocal}s can be removed (and thus deleted) or modified,
	 * we do not reference them here. Instead, if this <code>ArticlePrice</code> is
	 * created for a <code>NestedProductTypeLocal.innerProductType</code>, we store the
	 * <code>packageProductType</code> (i.e. the container) here and copy
	 * {@link NestedProductTypeLocal#getQuantity()} in {@link #innerProductTypeQuantity}.
	 */
	public ProductType getPackageProductType()
	{
		return packageProductType;
	}
	/**
	 * If {@link #packageProductType} is not <code>null</code>, this <code>ArticlePrice</code> has
	 * been created for the inner product[type] and the result of {@link NestedProductTypeLocal#getQuantity()}
	 * is copied here.
	 * If this is not created for a nested product[type], it defaults to 0. If it is created for nested
	 * products (not types!), it still contains the value copied from {@link NestedProductTypeLocal#getQuantity()}
	 * even though there is one instance of ArticlePrice for every Product.
	 */
	public int getInnerProductTypeQuantity()
	{
		return innerProductTypeQuantity;
	}
//	/**
//	 * @return Returns the nestedProductTypeLocal.
//	 */
//	public NestedProductTypeLocal getNestedProductType()
//	{
//		return nestedProductTypeLocal;
//	}
	/**
	 * @return Returns the origPrice.
	 */
	public org.nightlabs.jfire.accounting.Price getOrigPrice()
	{
		return origPrice;
	}
//	/**
//	 * <b>Warning!</b> You should never call this method! It is called during replication to a reseller-organisation.
//	 * <p>
//	 * This method sets the <code>origPrice</code> to <code>null</code>.
//	 * </p>
//	 */
//	public void __clearOrigPrice()
//	{
//		this.origPrice = null;
//	}
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
