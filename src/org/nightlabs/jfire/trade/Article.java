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

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Utils;

/**
 * An instance of <tt>Article</tt> occurs in the context of a {@link Segment}. It represents a
 * {@link Product} or a {@link ProductType} within an {@link Order}, an {@link Offer},
 * an {@link org.nightlabs.jfire.accounting.Invoice} or a {@link org.nightlabs.jfire.store.DeliveryNote}.
 * So you could understand an <tt>Article</tt> as a line on your <tt>Invoice</tt>, but depending
 * on the GUI, a whole <tt>Segment</tt> could be rendered differently, not showing the <tt>Article</tt>s
 * in detail.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.ArticleID"
 *		detachable="true"
 *		table="JFireTrade_Article"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, articleID"
 *
 * @jdo.fetch-group name="Article.articleLocal" fields="articleLocal"
 * @jdo.fetch-group name="Article.segment" fields="segment"
 * @jdo.fetch-group name="Article.productType" fields="productType"
 * @jdo.fetch-group name="Article.product" fields="product"
 * @jdo.fetch-group name="Article.tariff" fields="tariff"
 * @jdo.fetch-group name="Article.price" fields="price"
 * @jdo.fetch-group name="Article.reversedArticle" fields="reversedArticle"
 * @jdo.fetch-group name="Article.reversingArticle" fields="reversingArticle"
 * @jdo.fetch-group name="Article.order" fields="order"
 * @jdo.fetch-group name="Article.offer" fields="offer"
 * @jdo.fetch-group name="Article.invoice" fields="invoice"
 * @jdo.fetch-group name="Article.deliveryNote" fields="deliveryNote"
 * @jdo.fetch-group name="Article.delivery" fields="delivery"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="articleLocal, segment, productType, product, tariff, price"
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="segment, productType, product, tariff, price, order, offer, invoice, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="segment, productType, product, tariff, price, order, invoice, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="segment, productType, product, tariff, price, order, offer, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="segment, productType, product, tariff, price, order, offer, invoice"
 */
public class Article
	implements Serializable, DeleteCallback, DetachCallback, StoreCallback
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Article.class);

	public static final String FETCH_GROUP_ARTICLE_LOCAL = "Article.articleLocal";
	public static final String FETCH_GROUP_SEGMENT = "Article.segment";
	public static final String FETCH_GROUP_PRODUCT_TYPE = "Article.productType";
	public static final String FETCH_GROUP_PRODUCT = "Article.product";
	public static final String FETCH_GROUP_PRICE = "Article.price";
	public static final String FETCH_GROUP_REVERSED_ARTICLE = "Article.reversedArticle";
	public static final String FETCH_GROUP_REVERSING_ARTICLE = "Article.reversingArticle";
	public static final String FETCH_GROUP_ORDER = "Article.order";
	public static final String FETCH_GROUP_OFFER = "Article.offer";
	public static final String FETCH_GROUP_INVOICE = "Article.invoice";
	public static final String FETCH_GROUP_DELIVERY_NOTE = "Article.deliveryNote";
	public static final String FETCH_GROUP_DELIVERY = "Article.delivery";

	// the following fetch-groups are virtual and processed in the detach callback
	// all of them are included virtually in the FetchGroupsTrade.articleInXXX fetch-groups
	public static final String FETCH_GROUP_ORDER_ID = "Article.orderID";
	public static final String FETCH_GROUP_OFFER_ID = "Article.offerID";
	public static final String FETCH_GROUP_INVOICE_ID = "Article.invoiceID";
	public static final String FETCH_GROUP_DELIVERY_NOTE_ID = "Article.deliveryNoteID";
	public static final String FETCH_GROUP_REVERSED_ARTICLE_ID = "Article.reversedArticleID";
	public static final String FETCH_GROUP_REVERSING_ARTICLE_ID = "Article.reversingArticleID";
	public static final String FETCH_GROUP_VENDOR_ID = "Article.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "Article.customerID";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long articleID;

	public static long createArticleID()
	{
		return IDGenerator.nextID(Article.class.getName());
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ArticleLocal articleLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Segment segment;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Order order;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Offer offer;

	/**
	 * An <tt>Article</tt> can only be part of none or exactly one <tt>Invoice</tt>.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Invoice invoice = null;

	/**
	 * <b>Important change! New:</b>
	 * An <tt>Article</tt> can only be in one <tt>DeliveryNote</tt> (or in none).
	 * If the <tt>Article</tt> shall be returned, a new reverse-<tt>Article</tt>
	 * needs to be created. Hence, <tt>Invoice</tt> and <tt>DeliveryNote</tt> handling
	 * is quite the same.
	 * <p>
	 * <b>Old:</b>
	 * An <tt>Article</tt> can be part of many <tt>DeliveryNote</tt>s. This field reflects the
	 * last DeliveryNote in which this Article has been delivered. If the customer returns
	 * the product (e.g. because it is defective), this field is set to <tt>null</tt>
	 * again.
	 * <p>
	 * This means this field is always <tt>null</tt> when it waits to be included in a
	 * new deliveryNote. Whether it has been sent is then distinguished by the flag
	 * <tt>DeliveryNote.delivered</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryNote deliveryNote = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Product product = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private OrderID orderID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean orderID_detached = false;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private OfferID offerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean offerID_detached = false;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private InvoiceID invoiceID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean invoiceID_detached = false;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private DeliveryNoteID deliveryNoteID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean deliveryNoteID_detached = false;

	public OrderID getOrderID()
	{
		if (orderID == null && !orderID_detached)
			orderID = (OrderID) JDOHelper.getObjectId(order);

		return orderID;
	}
	public OfferID getOfferID()
	{
		if (offerID == null && !offerID_detached)
			offerID = (OfferID) JDOHelper.getObjectId(offer);

		return offerID;
	}
	public InvoiceID getInvoiceID()
	{
		if (invoiceID == null && !invoiceID_detached)
			invoiceID = (InvoiceID) JDOHelper.getObjectId(invoice);

		return invoiceID;
	}
	public DeliveryNoteID getDeliveryNoteID()
	{
		if (deliveryNoteID == null && !deliveryNoteID_detached)
			deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);

		return deliveryNoteID;
	}

	public ArticleID getReversedArticleID()
	{
		if (reversedArticleID == null && !reversedArticleID_detached)
			reversedArticleID = (ArticleID) JDOHelper.getObjectId(reversedArticle);

		return reversedArticleID;
	}
	public ArticleID getReversingArticleID()
	{
		if (reversingArticleID == null && !reversingArticleID_detached)
			reversingArticleID = (ArticleID) JDOHelper.getObjectId(reversingArticle);

		return reversingArticleID;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID vendorID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID customerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean customerID_detached = false;

	/**
	 * @return Returns the ID of the vendor, which is either obtained via {@link Order#getVendorID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = order.getVendorID();

		return vendorID;
	}

	/**
	 * @return Returns the ID of the customer, which is either obtained via {@link Order#getCustomerID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = order.getCustomerID();

		return customerID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private ArticlePrice price = null;

	/**
	 * This is set according to the result of PriceConfig.isDependentOnOffer().
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean priceDependentOnOffer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Tariff tariff = null;

	/**
	 * Whether or not this Article has already been reversed. This is <code>true</code>,
	 * if {@link #reversingArticle} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean reversed = false;

	/**
	 * Whether or not this Article reverses another one. This is <code>true</code>,
	 * if {@link #reversedArticle} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean reversing = false;

	/**
	 * If this <tt>Article</tt> reverses (i.e. refunds) a previously sold Article, this member points
	 * to the original Article.
	 *
	 * @see #reverse
	 * @see #reversingArticle
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Article reversedArticle = null;

	/**
	 * If this <tt>Article</tt> is reversed, the new Article (which reverses this one),
	 * will be referenced here.
	 *
	 * @see #reversedArticle
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @! mapped-by="reversedArticle"
	 */
	private Article reversingArticle = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private ArticleID reversedArticleID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean reversedArticleID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private ArticleID reversingArticleID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean reversingArticleID_detached = false;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Date autoReleaseDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allocationPending = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean releasePending = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allocated = false;

	/**
	 * This is usually <tt>null</tt> or the fully qualified class name of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String allocationExceptionClass = null;

	/**
	 * This is usually <tt>null</tt> or the exception message (no stacktrace) of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String allocationExceptionMessage = null;

	/**
	 * This is usually <tt>null</tt> or the result of {@link StackTraceElement#toString()}
	 * specifying where the root exception happened.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String allocationExceptionLocation = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allocationAbandoned = false;

	/**
	 * This is usually <tt>null</tt> or the fully qualified class name of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String releaseExceptionClass = null;

	/**
	 * This is usually <tt>null</tt> or the exception message (no stacktrace) of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String releaseExceptionMessage = null;

	/**
	 * This is usually <tt>null</tt> or the result of {@link StackTraceElement#toString()}
	 * specifying where the root exception happened.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String releaseExceptionLocation = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean releaseAbandoned = false;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Article() { }

	/**
	 * This method is used by all constructors to check common parameters and to
	 * initialize the related fields.
	 */
	protected void init(Offer offer, Segment segment, long articleID)
	{
		if (offer == null)
			throw new NullPointerException("offer");

		if (segment == null)
			throw new NullPointerException("segment");

		if (articleID < 0)
			throw new IllegalArgumentException("articleID < 0!");

		this.offer = offer;
		this.order = offer.getOrder();
		this.segment = segment;
		this.organisationID = offer.getOrganisationID();
		this.articleID = articleID;
		this.primaryKey = getPrimaryKey(organisationID, articleID);
		this.currency = offer.getCurrency();
	}

	/**
	 * This constructor is used to create a new Article which reverses a previously
	 * sold one.
	 * <p>
	 * Do not use this constructor directly, but call
	 * {@link Trader#reverseArticle(User, Offer, Article)}! <code>Trader</code>
	 * delegates to {@link #reverseArticle(User, Offer, long)} which means, the
	 * <code>reversedArticle</code> is in charge of creating its reversing <code>Article</code>.
	 */
	protected Article(User user, Offer offer, long articleID, Article reversedArticle)
	{
		init(offer, reversedArticle.getSegment(), articleID);

		if (reversedArticle == null)
			throw new IllegalArgumentException("reversedArticle == null!");

		if (!this.offer.getOrder().getPrimaryKey().equals(reversedArticle.getOffer().getOrder().getPrimaryKey()))
			throw new IllegalArgumentException("An article can only be refunded within the same order!");

		if (reversedArticle.reversingArticle != null)
			throw new IllegalArgumentException("The passed article (" + reversedArticle.getPrimaryKey() + ") cannot be reversed, because it has already been reversed before!");

		if (reversedArticle.reversedArticle != null)
			throw new IllegalArgumentException("The passed article (" + reversedArticle.getPrimaryKey() + ") cannot be reversed, because it is itself reversing another Article!");

		this.setReversedArticle(reversedArticle);
		reversedArticle.setReversingArticle(this);
		this.productType = reversedArticle.getProductType();
		this.product = reversedArticle.getProduct();
		this.tariff = reversedArticle.getTariff();
		this.price = reversedArticle.getPrice().createRefundPrice();

		// copy status
		this.allocated = reversedArticle.isAllocated();
		this.allocationPending = reversedArticle.isAllocationPending();
		this.releasePending = reversedArticle.isReleasePending();
	}

	/**
	 * This constructor is used to create an <tt>Article</tt> with a <tt>Product</tt>.
	 * The product will not be allocated by this action! This must be done by outside logic.
	 * <p>
	 * Do not use this constructor directly, but use the {@link Trader} to create an
	 * <tt>Article</tt>!
	 * <p>
	 * The new article will not yet have a price! This must be set afterwards to allow
	 * subclassing of <tt>Article</tt> and making prices dependent on the new fields
	 * of the subclass.
	 */
	public Article(Offer offer, Segment segment, long articleID, Product product, Tariff tariff)
	{
		this(offer, segment, articleID, product.getProductType(), product, tariff);
	}

	/**
	 * This constructor is used to create an <tt>Article</tt> with a <tt>ProductType</tt>, means
	 * the allocation of the <tt>Product</tt> will be done later (e.g. after the customer
	 * accepted the <tt>Offer</tt>).
	 * <p>
	 * Do not use this constructor directly, but use the {@link Trader} to create an
	 * <tt>Article</tt>!
	 * <p>
	 * The new article will not yet have a price! This must be set afterwards to allow
	 * subclassing of <tt>Article</tt> and making prices dependent on the new fields
	 * of the subclass.
	 */
	public Article(Offer offer, Segment segment, long articleID, ProductType productType, Tariff tariff)
	{
		this(offer, segment, articleID, productType, null, tariff);
	}

	/**
	 * @param offer must not be null
	 * @param segment must not be null
	 * @param articleID must be >= 0
	 * @param productType must not be null
	 * @param product can be null
	 * @param currency must not be null
	 */
	protected Article(
			Offer offer, Segment segment, long articleID, ProductType productType,
			Product product, Tariff tariff)
	{
		init(offer, segment, articleID);

		if (productType == null)
			throw new NullPointerException("productType");

		if (productType.isPackageInner()) // TODO wie machen wir das bei inter-organisations-handel? Wir MÜSSEN innerproducts handeln können!
			throw new IllegalArgumentException("productType \""+productType.getPrimaryKey()+"\" is an inner part of a package! Cannot make an article out of it!");

		IPackagePriceConfig packagePriceConfig = productType.getPackagePriceConfig();
		if (packagePriceConfig == null)
			throw new IllegalStateException("packagePriceConfig of productType \""+productType.getPrimaryKey()+"\" is undefined!");

		this.productType = productType;
		this.product = product;
		this.tariff = tariff;

//		PersistenceManager pm = JDOHelper.getPersistenceManager(offer);
//		if (pm == null)
//			throw new IllegalStateException("offer is currently not persistent! Can only create an article with a persistent offer.");
//
//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

//		this.price = new ArticlePrice(
//				packagePriceConfig.getArticlePrice(this),
//				accounting.getOrganisationID(),
//				accountingPriceConfig.getPriceConfigID(),
//				accountingPriceConfig.createPriceID(), false);
		// The price is set after creation to allow subclassing of Article and making the
		// price dependent on new fields of the subclass.
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the articleID.
	 */
	public long getArticleID()
	{
		return articleID;
	}
	public static String getPrimaryKey(
			String organisationID, long articleID)
	{
		return organisationID + '/' + Long.toHexString(articleID);
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}
	/**
	 * @param productType The productType to set.
	 */
	protected void setProductType(ProductType productType)
	{
		this.productType = productType;
	}
	/**
	 * @return Returns the product.
	 */
	public Product getProduct()
	{
		return product;
	}
	/**
	 * @return Returns the price.
	 */
	public ArticlePrice getPrice()
	{
		return price;
	}
	/**
	 * @param price The price to set.
	 */
	public void setPrice(ArticlePrice price)
	{
		// WORKAROUND JPOX should remove dependent objects, but currently it doesn't
		if (this.price != null) {
			PersistenceManager pm = getPersistenceManager();
			if (pm == null)
				throw new IllegalStateException("I thought, this method would only be called in the server?! Marco.");

			ArticlePrice oldPrice = this.price;
			this.price = null;
			pm.deletePersistent(oldPrice);
		}

		this.price = price;
	}
	/**
	 * @return Returns the priceDependentOnOffer.
	 */
	public boolean isPriceDependentOnOffer()
	{
		return priceDependentOnOffer;
	}
	/**
	 * @param priceDependentOnOffer The priceDependentOnOffer to set.
	 */
	protected void setPriceDependentOnOffer(boolean priceDependentOnOffer)
	{
		this.priceDependentOnOffer = priceDependentOnOffer;
	}
	/**
	 * @return Returns the offer.
	 */
	public Offer getOffer()
	{
		return offer;
	}

	/**
	 * @return Returns either <code>null</code> or the <code>Article</code> that is reversed by this instance.
	 */
	public Article getReversedArticle()
	{
		return reversedArticle;
	}
	/**
	 * @return Returns either <code>null</code> or the <code>Article</code> which reverses this instance.
	 */
	public Article getReversingArticle()
	{
		return reversingArticle;
	}

//	/**
//	 * @return Returns the allocated.
//	 */
//	public boolean isAllocated()
//	{
//		return allocated;
//	}
	/**
	 * @return Returns the deliveryNote.
	 */
	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}
	/**
	 * @param deliveryNote The deliveryNote to set.
	 */
	public void setDeliveryNote(DeliveryNote deliveryNote)
	{
		this.deliveryNote = deliveryNote;
	}
	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment()
	{
		return segment;
	}
	/**
	 * @param segment The segment to set.
	 */
	protected void setSegment(Segment segment)
	{
		this.segment = segment;
	}
//	/**
//	 * This method returns the date and time when the Article will
//	 * automatically be released. To change this, you need to call
//	 * <tt>allocate(..)</tt>.
//	 *
//	 * @return Returns the autoReleaseDT.
//	 * @see #allocate(User, Date)
//	 */
//	public Date getAutoReleaseDT()
//	{
//		return autoReleaseDT;
//	}

	// TODO logic for allocate and release must be in the Trader (and not here)
//	/**
//	 * This method allocates the product which is embraced by this Article.
//	 * The allocation work is delegated to the Trader, because it is necessary
//	 * to create Order-s & Offer-s to get the requirement-products.
//	 * @throws ModuleException
//	 */
//	public void allocate(User user, Date autoReleaseDT)
//			throws ModuleException
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of Article is currently not persistent!");
//		
//		Trader trader = Trader.getTrader(pm);
//		trader.allocateArticle(user, this, autoReleaseDT);
//
//		this.autoReleaseDT = autoReleaseDT;
//		this.allocated = true;
//	}
//	
//	public void release(User user)
//			throws ModuleException
//	{
//		if (!allocated)
//			return;
//
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of Article is currently not persistent!");
//
//		Trader trader = Trader.getTrader(pm);
//		trader.releaseArticle(user, this);
//		this.allocated = false;
//	}

	public Invoice getInvoice() {
		return invoice;
	}
	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	/**
	 * @return Returns the tariff.
	 */
	public Tariff getTariff()
	{
		return tariff;
	}
	/**
	 * @param tariff The tariff to set.
	 */
	public void setTariff(Tariff tariff)
	{
		this.tariff = tariff;
	}
	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	public boolean isAllocationPending()
	{
		return allocationPending;
	}
	protected void _setAllocationPending(boolean allocationPending)
	{
		this.allocationPending = allocationPending;
	}
	protected void setAllocationPending(boolean allocationPending)
	{
		this.allocationPending = allocationPending;

		if (isReversed())
			getReversingArticle()._setAllocationPending(allocationPending);

		if (isReversing())
			getReversedArticle()._setAllocationPending(allocationPending);
	}
	public boolean isReleasePending()
	{
		return releasePending;
	}

	protected void _setReleasePending(boolean releasePending)
	{
		this.releasePending = releasePending;
	}

	protected void setReleasePending(boolean releasePending)
	{
		this.releasePending = releasePending;

		if (isReversed())
			getReversingArticle()._setReleasePending(releasePending);

		if (isReversing())
			getReversedArticle()._setReleasePending(releasePending);
	}
	public boolean isAllocated()
	{
		return allocated;
	}
	protected void _setAllocated(boolean allocated)
	{
		this.allocated = allocated;
	}
	protected void setAllocated(boolean allocated)
	{
		this.allocated = allocated;

		if (isReversed())
			getReversingArticle()._setAllocated(allocated);

		if (isReversing())
			getReversedArticle()._setAllocated(allocated);
	}

	/**
	 * @return Returns the order.
	 */
	public Order getOrder()
	{
		return order;
	}

	/**
	 * This method is called by {@link Trader#reverseArticles(User, Offer, Collection)}.
	 * When inheriting <code>Article</code>, you MUST override this method, because it
	 * MUST return an instance of the same class as the reversed <code>Article</code>.
	 * <p>
	 * Override this method and create a new instance of your <code>Article</code> implementation
	 * which reverses this Article (if possible).
	 * </p>
	 * <p>
	 * This method should be a one-liner! You should put all your logic into the constructor
	 * (see {@link #Article(User, Offer, long, Article)}).
	 * </p>
	 * @param user The responsible user. 
	 * @param offer The <code>Offer</code> into which the new <code>Article</code> will be created.
	 * @param articleID A new (not yet used) unique id within the current organisation for the new <code>Article</code>, that will be created.
	 * @return Returns the new Article that reverses this one.
	 */
	protected Article reverseArticle(User user, Offer offer, long articleID)
	{
		if (this.getClass() != Article.class)
			throw new IllegalStateException("You did not override Article.reverseArticle(...)! When inheriting Article, you must override this method and return an instance of your new class!");

		return new Article(user, offer, articleID, this);
	}

	protected void setReversedArticle(Article reversedArticle)
	{
		this.reversedArticle = reversedArticle;
		this.reversing = reversedArticle != null;
	}
	protected void setReversingArticle(Article reversingArticle)
	{
		this.reversingArticle = reversingArticle;
		this.reversed = reversingArticle != null;
	}

	public boolean isReversed()
	{
		return reversed;
	}
	public boolean isReversing()
	{
		return reversing;
	}

	public void jdoPreDelete()
	{
		if (reversedArticle != null)
			reversedArticle.setReversingArticle(null);

		ArticleLocal tmpArticleLocal = this.articleLocal;
		this.articleLocal = null;
		getPersistenceManager().deletePersistent(tmpArticleLocal);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Article has currently no PersistenceManager assigned!");

		return pm;
	}

	public void jdoPreDetach()
	{
	}

	public void jdoPostDetach(Object _attached)
	{
		Article attached = (Article)_attached;
		Article detached = this;
		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		boolean fetchGroupsArticleInEditor =
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_ORDER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_OFFER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_INVOICE_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_DELIVERY_NOTE_EDITOR);

// The following lines (nulling all the IDs) is not really necessary, but in case the JDO impl
// has a bug that causes them to be copied, we're sure they're always null - despite of any bug
		detached.orderID = null;
		detached.offerID = null;
		detached.invoiceID = null;
		detached.deliveryNoteID = null;

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_ORDER_ID)) {
			detached.orderID = attached.getOrderID();
			detached.orderID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_OFFER_ID)) {
			detached.offerID = attached.getOfferID();
			detached.offerID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_INVOICE_ID)) {
			detached.invoiceID = attached.getInvoiceID();
			detached.invoiceID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_DELIVERY_NOTE_ID)) {
			detached.deliveryNoteID = attached.getDeliveryNoteID();
			detached.deliveryNoteID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_REVERSED_ARTICLE_ID)) {
			detached.reversedArticleID = attached.getReversedArticleID();
			detached.reversedArticleID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_REVERSING_ARTICLE_ID)) {
			detached.reversingArticleID = attached.getReversingArticleID();
			detached.reversingArticleID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}
	}

	public void jdoPreStore()
	{
		if (primaryKey == null) {
			primaryKey = getPrimaryKey(organisationID, articleID);
			logger.info("Seems, the JPOX bug still exists: primaryKey == null! Resetting it to " + primaryKey);
		}
	}

	public String toString()
	{
		return this.getClass().getName() + '{' + getPrimaryKey() + '}';
	}

	public void setAllocationException(Throwable t)
	{
		if (t == null) {
			this.allocationExceptionClass = null;
			this.allocationExceptionMessage = null;
			this.allocationExceptionLocation = null;
			return;
		}

		Throwable root = ExceptionUtils.getRootCause(t);
		if (root == null)
			root = t;
		StackTraceElement ste = root.getStackTrace()[0];
//		while (root.getCause() != null)
//			root = root.getCause();

		this.allocationExceptionClass = root.getClass().getName();
		this.allocationExceptionMessage = root.getLocalizedMessage();
		this.allocationExceptionLocation = ste.toString();
	}

	public void setReleaseException(Throwable t)
	{
		if (t == null) {
			this.releaseExceptionClass = null;
			this.releaseExceptionMessage = null;
			this.releaseExceptionLocation = null;
			return;
		}

		Throwable root = ExceptionUtils.getRootCause(t);
		if (root == null)
			root = t;
		StackTraceElement ste = root.getStackTrace()[0];

		this.releaseExceptionClass = root.getClass().getName();
		this.releaseExceptionMessage = root.getLocalizedMessage();
		this.releaseExceptionLocation = ste.toString();
	}

	public String getAllocationExceptionClass()
	{
		return allocationExceptionClass;
	}
	public String getAllocationExceptionMessage()
	{
		return allocationExceptionMessage;
	}
	public String getAllocationExceptionLocation()
	{
		return allocationExceptionLocation;
	}
	public boolean isAllocationAbandoned()
	{
		return allocationAbandoned;
	}
	public void setAllocationAbandoned(boolean allocationAbandoned)
	{
		this.allocationAbandoned = allocationAbandoned;
	}

	public String getReleaseExceptionClass()
	{
		return releaseExceptionClass;
	}
	public String getReleaseExceptionMessage()
	{
		return releaseExceptionMessage;
	}
	public String getReleaseExceptionLocation()
	{
		return releaseExceptionLocation;
	}
	public boolean isReleaseAbandoned()
	{
		return releaseAbandoned;
	}
	public void setReleaseAbandoned(boolean releaseAbandoned)
	{
		this.releaseAbandoned = releaseAbandoned;
	}

	public ArticleLocal getArticleLocal()
	{
		return articleLocal;
	}
	protected void setArticleLocal(ArticleLocal articleLocal)
	{
		this.articleLocal = articleLocal;
	}

	protected ArticleLocal createArticleLocal(User user)
	{
		return new ArticleLocal(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Article))
			return false;

		Article o = (Article)obj;

		return
				Utils.equals(this.organisationID, o.organisationID) &&
				this.articleID == o.articleID;
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) ^ Utils.hashCode(articleID);
	}
}
