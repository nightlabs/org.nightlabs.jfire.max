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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.AccountingPriceConfig;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Utils;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OfferID"
 *		detachable="true"
 *		table="JFireTrade_Offer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, offerIDPrefix, offerID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *
 * @jdo.query name="getNonFinalizedOffersForOrder" query="SELECT
 *			WHERE this.order == paramOrder && this.finalizeDT == null
 *			PARAMETERS Order paramOrder
 *			import org.nightlabs.jfire.trade.Order
 *			ORDER BY offerID DESCENDING"
 *
 * @jdo.fetch-group name="Offer.offerLocal" fields="offerLocal"
 * @jdo.fetch-group name="Offer.price" fields="price"
 * @jdo.fetch-group name="Offer.articles" fields="articles"
 * @jdo.fetch-group name="Offer.currency" fields="currency"
 * @jdo.fetch-group name="Offer.order" fields="order"
 * @jdo.fetch-group name="Offer.createUser" fields="createUser"
 * @jdo.fetch-group name="Offer.this" fetch-groups="default" fields="offerLocal, order, currency, articles, price"
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleEdit" fetch-groups="default" fields="articles"
 */
public class Offer
implements
		Serializable,
		ArticleContainer, // TODO shall we implement SegmentContainer here? Do we need it?
		DetachCallback,
		AttachCallback
{
	public static final String FETCH_GROUP_OFFER_LOCAL = "Offer.offerLocal";
	public static final String FETCH_GROUP_PRICE = "Offer.price";
	public static final String FETCH_GROUP_ARTICLES = "Offer.articles";
	public static final String FETCH_GROUP_CURRENCY = "Offer.currency";
	public static final String FETCH_GROUP_ORDER = "Offer.order";
	public static final String FETCH_GROUP_CREATE_USER = "Offer.createUser";
	public static final String FETCH_GROUP_THIS_OFFER = "Offer.this";

	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_VENDOR_ID = "Offer.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "Offer.customerID";

	/**
	 * @return a <tt>Collection</tt> of <tt>Offer</tt>
	 */
	public static Collection getNonFinalizedOffers(PersistenceManager pm, Order order)
	{
		Query query = pm.newNamedQuery(Offer.class, "getNonFinalizedOffersForOrder");
		return (Collection) query.execute(order);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String offerIDPrefix;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long offerID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private OfferLocal offerLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Order order;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

//	/**
//	 * key: String invoicePK<br/>
//	 * value: Invoice invoice
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="org.nightlabs.jfire.accounting.Invoice"
//	 *		@!mapped-by="offer"
//	 *
//	 * @jdo.join
//	 *
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map invoices = new HashMap();

	/**
	 * An Offer is stable, if it does not change values when prices are recalculated.
	 * Only stable Offers can be confirmed. An instable Offer can come into existence
	 * if conflicting PriceConfigs of two or more OfferItems interfere.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean stable;

	/**
	 * An Offer is only valid if it is stable and has been validated. It needs to be
	 * valid, before it can be finalized.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean valid = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser;

	/**
	 * An <tt>Offer</tt> is finalized, before it is sent on an asynchronous way (e.g. letter,
	 * email) to the customer. This avoids communication conflicts as it forces the generation
	 * of a new <tt>Offer</tt> if the customer requests changes after reception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date finalizeDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User finalizeUser = null;

//	/**
//	 * key: String articlePK<br/>
//	 * value: Article article
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="String"
//	 *		value-type="Article"
//	 *		dependent-value="true"
//	 *		mapped-by="offer"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 */
//	private Map articles;

	/**
	 * Instances of {@link Article}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Article"
	 *		dependent-value="true"
	 *		mapped-by="offer"
	 */
	private Set articles;

	/**
	 * This member represents the sum of all prices of all offer items.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price price;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean containsPricesDependentOnOffer = false;

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
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.OfferActionHandler"
	 *		table="JFireTrade_Offer_offerActionHandlers"
	 *
	 * @jdo.join
	 */
	private Set offerActionHandlers;

	/**
	 * @deprecated This constructor exists only for JDO!
	 */
	protected Offer() { }

	public Offer(User user, Order order, String offerIDPrefix, long offerID)
	{
		if (order == null)
			throw new IllegalArgumentException("order must not be null!");

		ObjectIDUtil.assertValidIDString(offerIDPrefix, "offerIDPrefix");

		if (offerID < 0)
			throw new IllegalArgumentException("offerID < 0");

		PersistenceManager pm = JDOHelper.getPersistenceManager(order);
		if (pm == null)
			throw new IllegalStateException("order is not persistent! Could not get a PersistenceManager from it!");

		Accounting accounting = Accounting.getAccounting(pm);

		this.order = order;
		this.organisationID = order.getOrganisationID();
		this.offerIDPrefix = offerIDPrefix;
		this.offerID = offerID;
		this.currency = order.getCurrency();
		this.primaryKey = getPrimaryKey(this.organisationID, this.offerIDPrefix, this.offerID);
		this.createDT = new Date();
		this.createUser = user;

		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();
		this.price = new Price(
				accountingPriceConfig.getOrganisationID(), accountingPriceConfig.getPriceConfigID(),
				accountingPriceConfig.createPriceID(), getCurrency());

		articles = new HashSet();
		offerActionHandlers = new HashSet();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getOfferIDPrefix()
	{
		return offerIDPrefix;
	}
	/**
	 * @return Returns the offerID.
	 */
	public long getOfferID()
	{
		return offerID;
	}
	public static String getPrimaryKey(String organisationID, String offerIDPrefix, long offerID)
	{
		return organisationID + '/' + offerIDPrefix + '/' + Long.toHexString(offerID);
	}

	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public OfferLocal getOfferLocal()
	{
		return offerLocal;
	}
	protected void setOfferLocal(OfferLocal offerLocal)
	{
		this.offerLocal = offerLocal;
	}

	public void addArticles(Collection articles)
	{
		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot add a new Article!");

		// Whenever an Offer is changed, it needs to be marked as not valid to force a new validation!
		this.valid = false;

		for (Iterator iter = articles.iterator(); iter.hasNext();) {
			Article article = (Article) iter.next();

			if (!this.primaryKey.equals(article.getOffer().getPrimaryKey()))
				throw new IllegalArgumentException("offerItem.offer != this!");

			this.articles.add(article);
			this.order.addArticle(article);
		}
	}

	public void addArticle(Article article)
	{
		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot add a new Article!");

		// Whenever an Offer is changed, it needs to be marked as not valid to force a new validation!
		this.valid = false;

		if (!JDOHelper.getObjectId(this).equals(article.getOfferID()))
			throw new IllegalArgumentException("article.offer != this!");

		this.articles.add(article);

		if (JDOHelper.isDetached(this))
			attachable = false;
		else
			this.order.addArticle(article);
	}

	public void removeArticle(Article article)
	{
		if (isFinalized())
			throw new IllegalStateException("Offer \""+getPrimaryKey()+"\" is already finalized! Cannot delete the Article \""+article.getPrimaryKey()+"\"!");

		if (!article.isReversing() && (article.isAllocated() || article.isAllocationPending()))
			throw new IllegalStateException("Article \""+article.getPrimaryKey()+"\" is allocated (or allocationPending) and not reversing! Cannot be removed!");

		this.articles.remove(article);
		this.valid = false;

		if (JDOHelper.isDetached(this))
			attachable = false;
		else {
			// WORKAROUND JPOX should have deleted the Article automatically, because dependent=true, but it doesn't work :-(
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm != null) { // only in the server - this method is called in the client, too.
				pm.deletePersistent(article);
			}
			// end workaround
		}
	}

	/**
	 * This method recalculates all prices for all OfferItems. To be sure there's no
	 * indefinite price definition, it does it twice
	 * (if there's a price config marked as isDependentOnOffer()=true).
	 */
	public synchronized void calculatePrice()
	{
//	 TODO put this logic into the bean?!
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Offer is not persistent! Cannot calculate the price!");

		Trader trader = Trader.getTrader(pm);
		if (trader.getOrganisationID().equals(this.getOrganisationID())) {
			calculateArticlePrices(false);
			long firstNewAmount = price.getAmount();
			if (containsPricesDependentOnOffer) {
				// we recalculate a second time to make sure we have no indefinite price.
				calculateArticlePrices(false);
				long secondNewAmount = price.getAmount();
				this.stable = secondNewAmount == firstNewAmount;
			}
			else
				this.stable = true;
		} // if (trader.getOrganisationID().equals(this.getOrganisationID())) {
		else {
			// The offer is not coming from here locally, but from another organisation (maybe on different server).
			// Thus, we need to delegate the price calculation to the other organisation.

			// TODO delegate to remote org
			throw new UnsupportedOperationException("NYI");
		}
	}

	/**
	 * This method calculates all offerItem's prices and the offer's price. It's a local helper method
	 * called by calculatePrice().
	 * 
	 * @param all If all=false, it does not recalculate the price of an Article,
	 *	if Article.isPriceDependentFromOffer() returns false.
	 *	If all=true, all prices of all items are recalculated. 
	 */
	private void calculateArticlePrices(boolean all)
	{
		price.clearFragments();
		price.setAmount(0);
		containsPricesDependentOnOffer = false;
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of offer is currently not persistent!");

		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article)it.next();
			if (all || article.isPriceDependentOnOffer()) {
				if (article.isPriceDependentOnOffer())
					containsPricesDependentOnOffer = true;

				ProductType productType = article.getProductType();
				article.setPriceDependentOnOffer(productType.getPackagePriceConfig().isDependentOnOffer());

				ArticlePrice articlePrice = article.getPrice();
				if (articlePrice != null) {
					article.setPrice(null);
					pm.deletePersistent(articlePrice);
					articlePrice = null;
				}

				article.setPrice(productType.getPackagePriceConfig().createArticlePrice(article));
			} // if (all || offerItem.isPriceDependentOnOffer()) {
			price.sumPrice(article.getPrice());
		}
	}

	/**
	 * @return Returns the order.
	 */
	public Order getOrder()
	{
		return order;
	}

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

	public Date getCreateDT()
	{
		return createDT;
	}
	public User getCreateUser()
	{
		return createUser;
	}

	/**
	 * @return Returns whether it's finalized.
	 */
	public boolean isFinalized()
	{
		return finalizeDT != null;
	}

	/**
	 * This method sets the finalized flag of an <tt>Offer</tt> to true. After that,
	 * a change of an <tt>Offer</tt> is not possible anymore.
	 * <p>
	 * After creation of an <tt>Offer</tt>, the next step in the service-customer relation
	 * is the finalization of the <tt>Offer</tt>. The customer receives a finalized offer
	 * if the communication works offline. If online, the customer can see each change
	 * of his "shopping cart" expressed by the current offer. But offline, it is necessary
	 * to ensure, that the customer finally confirms the correct Offer (and not one that
	 * has already been changed). Hence, the customer receives the Offer (on paper or
	 * by email) not before it has been finalized.
	 */
	protected void setFinalized(User user)
	{
		if (isFinalized())
			return;

		if (!isValid())
			throw new IllegalStateException("This Offer ("+getPrimaryKey()+") cannot be confirmed, because it is not valid! Call 'validate()' first and ensure the Offer is stable!");

		finalizeDT = new Date();
		finalizeUser = user;
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set _articles = null;

	public Collection getArticles()
	{
		if (_articles == null)
			_articles = Collections.unmodifiableSet(articles);

		return _articles;
	}

	/**
	 * This method must be called, before the Offer can be confirmed. If the Offer is changed
	 * (e.g. by <tt>addArticle(..)</tt>) the <tt>valid</tt> status is reset and <tt>validate()</tt>
	 * must be called again.
	 * <br/><br/>
	 * This method causes all prices to be recalculated and all products to be allocated (if they have
	 * been released by timeout).
	 * <p>
	 * 
	 * </p>
	 *
	 * @return
	 */
	protected boolean validate()
	{
		if (valid)
			return true;

//		valid = false;

		// TODO here, we should make sure, we have requirement-offers, which can
		// be fulfilled.
		
		// if (all requirement-offers are valid)
		boolean _valid = true;

		calculatePrice();
		if (!isStable())
			_valid = false;

		this.valid = _valid;
		return isValid();
	}

	/**
	 * An Offer is stable, if PriceConfig-s do not interfere and the prices do not
	 * change on calculation, if the Offer is not changed.
	 *
	 * @return Returns the stable.
	 */
	public boolean isStable()
	{
		return stable;
	}

	/**
	 * An Offer is only valid if it is stable and has been validated. It needs to be
	 * valid, before it can be finalized.
	 *
	 * @return Returns the valid.
	 */
	public boolean isValid()
	{
		if (valid && !stable) {
//		 TODO we should log an error here, because this should never happen!
			return false;		
		}
		return valid;
	}

	/**
	 * Sets valid
	 */
	protected void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public Date getFinalizeDT()
	{
		return finalizeDT;
	}
	public User getFinalizeUser()
	{
		return finalizeUser;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean attachable = true;

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Order is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public void jdoPreDetach()
	{
	}

	public void jdoPostDetach(Object _attached)
	{
		Offer attached = (Offer)_attached;
		Offer detached = this;
		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}
		detached.attachable = true;
	}

	public void jdoPreAttach()
	{
		if (!attachable)
			throw new IllegalStateException("This offer became non-attachable!");
	}

	public void jdoPostAttach(Object arg0)
	{
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set _offerActionHandlers = null;

	/**
	 * @return Instances of {@link OfferActionHandler}.
	 */
	@SuppressWarnings("unchecked")
	public Set getOfferActionHandlers()
	{
		if (_offerActionHandlers == null)
			_offerActionHandlers = Collections.unmodifiableSet(offerActionHandlers);

		return _offerActionHandlers;
	}

	@SuppressWarnings("unchecked")
	public void addOfferActionHandler(OfferActionHandler offerActionHandler)
	{
		if (!offerActionHandlers.contains(offerActionHandler))
			offerActionHandlers.add(offerActionHandler);
	}

	public boolean removeOfferActionHandler(OfferActionHandler offerActionHandler)
	{
		return offerActionHandlers.remove(offerActionHandler);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Offer))
			return false;

		Offer o = (Offer)obj;
		return
				Utils.equals(this.organisationID, o.organisationID) &&
				Utils.equals(this.offerIDPrefix, o.offerIDPrefix) &&
				this.offerID == o.offerID;
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) ^ Utils.hashCode(offerIDPrefix) ^ Utils.hashCode(offerID);
	}
}
