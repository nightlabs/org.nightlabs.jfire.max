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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Utils;


/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OrderID"
 *		detachable="true"
 *		table="JFireTrade_Order"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, orderIDPrefix, orderID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *
 * @!jdo.query
 *		name="getOrdersByVendorAndCustomer"
 *		query="SELECT
 *			WHERE JDOHelper.getObjectId(vendor) == paramVendorID &&
 *			      JDOHelper.getObjectId(customer) == paramCustomerID
 *			PARAMETERS AnchorID paramVendorID, AnchorID paramCustomerID
 *			import org.nightlabs.jfire.transfer.id.AnchorID"
 *
 * @jdo.query
 *		name="getOrderIDsByVendorAndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY orderID DESC"
 *
 * @!jdo.query
 *		name="getQuickSaleWorkOrderIDCandidates"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE
 *						quickSaleWorkOrder &&
 *						orderIDPrefix == :orderIDPrefix &&
 *						currency.currencyID == :currencyID &&
 *						vendor.organisationID == :paramVendorID_organisationID &&
 *            vendor.anchorID == :paramVendorID_anchorID &&
 *			      customer.organisationID == :paramCustomerID_organisationID &&
 *            customer.anchorID == :paramCustomerID_anchorID &&
 *            createUser.organisationID == :paramCreateUser_organisationID &&
 *            createUser.userID == :paramCreateUser_userID &&
 *            !offers.contains(finalizedOffer) && finalizedOffer.finalizeDT != null &&
 *            articles.isEmpty()
 *			ORDER BY orderID ASC"
 *
 * // TODO Switch to the code above when the JPOX bug is fixed: as soon as this.offers or this.articles is included in the WHERE block, the criteria above (at least this.quickSaleWorkOrder) are ignored.
 * @jdo.query
 *		name="getQuickSaleWorkOrderIDCandidates_WORKAROUND"
 *		query="SELECT
 *			WHERE
 *						this.quickSaleWorkOrder &&
 *						this.orderIDPrefix == :orderIDPrefix &&
 *						this.currency.currencyID == :currencyID &&
 *						this.vendor.organisationID == :paramVendorID_organisationID &&
 *            this.vendor.anchorID == :paramVendorID_anchorID &&
 *			      this.customer.organisationID == :paramCustomerID_organisationID &&
 *            this.customer.anchorID == :paramCustomerID_anchorID
 *			ORDER BY orderID ASC"
 *
 * @jdo.fetch-group name="Order.vendor" fields="vendor"
 * @jdo.fetch-group name="Order.currency" fields="currency"
 * @jdo.fetch-group name="Order.customer" fields="customer"
 * @jdo.fetch-group name="Order.customerGroup" fields="customerGroup"
 * @jdo.fetch-group name="Order.articles" fields="articles"
 * @jdo.fetch-group name="Order.segments" fields="segments"
 * @jdo.fetch-group name="Order.offers" fields="offers"
 * @jdo.fetch-group name="Order.createUser" fields="createUser"
 * @jdo.fetch-group name="Order.changeUser" fields="changeUser"
 * @jdo.fetch-group name="Order.this" fetch-groups="default" fields="vendor, currency, customer, customerGroup, articles, segments, offers, createUser, changeUser"
 */
public class Order
implements Serializable, ArticleContainer, SegmentContainer, DetachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_VENDOR = "Order.vendor";
	public static final String FETCH_GROUP_CUSTOMER = "Order.customer";
	public static final String FETCH_GROUP_CUSTOMER_GROUP = "Order.customerGroup";
	public static final String FETCH_GROUP_CURRENCY = "Order.currency";
	public static final String FETCH_GROUP_ARTICLES = "Order.articles";
	public static final String FETCH_GROUP_SEGMENTS = "Order.segments";
	public static final String FETCH_GROUP_OFFERS = "Order.offers";
	public static final String FETCH_GROUP_CREATE_USER = "Order.createUser";
	public static final String FETCH_GROUP_CHANGE_USER = "Order.changeUser";
	public static final String FETCH_GROUP_THIS_ORDER = "Order.this";

	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_VENDOR_ID = "Order.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "Order.customerID";

	/**
	 * This method queries all <code>Order</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param pm The PersistenceManager to be used for accessing the datastore.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Order}.
	 */
	@SuppressWarnings("unchecked")
	public static List<OrderID> getOrderIDs(PersistenceManager pm, AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		Query query = pm.newNamedQuery(Order.class, "getOrderIDsByVendorAndCustomer");
//		return (Collection) query.execute(vendorID, customerID);
// WORKAROUND JDOQL with ObjectID doesn't work yet.
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
			query.setRange(rangeBeginIdx, rangeEndIdx);

		return (List<OrderID>) query.executeWithMap(params);
	}

	@SuppressWarnings("unchecked")
	public static List<OrderID> getQuickSaleWorkOrderIDCandidates(
			PersistenceManager pm, AnchorID vendorID, AnchorID customerID,
			UserID createUserID, String orderIDPrefix, String currencyID)
	{
		Query q = pm.newNamedQuery(Order.class, "getQuickSaleWorkOrderIDCandidates_WORKAROUND");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);
		params.put("paramCreateUser_organisationID", createUserID.organisationID);
		params.put("paramCreateUser_userID", createUserID.userID);
		params.put("orderIDPrefix", orderIDPrefix);
		params.put("currencyID", currencyID);
		Collection<Order> orders = (Collection<Order>) q.executeWithMap(params);
		List<OrderID> orderIDs = new ArrayList<OrderID>();
		for (Iterator<Order> it = orders.iterator(); it.hasNext(); ) {
			Order order = it.next();

			if (!order.getArticles().isEmpty())
				continue;

			boolean skipOrder = false;
			for (Offer offer : order.getOffers()) {
				if (offer.isFinalized())
					skipOrder = true;
			}
			if (skipOrder)
				continue;

			orderIDs.add((OrderID) JDOHelper.getObjectId(order));
		}
		return orderIDs;

// TODO switch to the real query as soon as the jpox bug is fixed. Currently we cannot query on this.offers or this.articles

//		Query q = pm.newNamedQuery(Order.class, "getQuickSaleWorkOrderIDCandidates");
//
////	 WORKAROUND JDOQL with ObjectID doesn't work yet.
//		Map params = new HashMap();
//		params.put("paramVendorID_organisationID", vendorID.organisationID);
//		params.put("paramVendorID_anchorID", vendorID.anchorID);
//		params.put("paramCustomerID_organisationID", customerID.organisationID);
//		params.put("paramCustomerID_anchorID", customerID.anchorID);
//		params.put("paramCreateUser_organisationID", createUserID.organisationID);
//		params.put("paramCreateUser_userID", createUserID.userID);
//		params.put("orderIDPrefix", orderIDPrefix);
//		params.put("currencyID", currencyID);
//
////		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
////			q.setRange(rangeBeginIdx, rangeEndIdx);
//
//		return (List<OrderID>) q.executeWithMap(params);
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
	private String orderIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long orderID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private OrganisationLegalEntity vendor;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private LegalEntity customer;

	/**
	 * Because the {@link #customer} may have many available <tt>CustomerGroup</tt>s,
	 * it is necessary to define which one shall be used for this <tt>Order</tt>.  
	 *
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private CustomerGroup customerGroup;

	/**
	 * After the first payment or delivery, the customer is not changeable anymore.
	 * The same applies to the {@link #customerGroup}.
	 */
	protected boolean customerChangeable = true;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean quickSaleWorkOrder;

//	/**
//	 * key: String articlePK<br/>
//	 * value: Article article
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Article"
//	 *		mapped-by="order"
//	 *		dependent-value="true"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 *
//	 * @!jdo.join
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map articles = new HashMap();

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Article"
	 *		mapped-by="order"
	 *		dependent-value="true"
	 */
	private Set<Article> articles;

//	/**
//	 * key: String offerPK<br/>
//	 * value: Offer offer
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Offer"
//	 *		mapped-by="order"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 *
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map offers = new HashMap();

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Offer"
	 *		mapped-by="order"
	 */
	private Set<Offer> offers;

//	/**
//	 * key: String segmentPK<br/>
//	 * value: Segment segment
//	 * <br/><br/>
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Segment"
//	 *		mapped-by="order"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 *
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map segments = new HashMap();

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Segment"
	 *		mapped-by="order"
	 */
	private Set<Segment> segments;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date changeDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User changeUser;	

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

	public Order() {}

	public Order(
			OrganisationLegalEntity vendor, LegalEntity customer,
			String orderIDPrefix, long orderID,
			Currency currency, User user)
	{
		if (vendor == null)
			throw new IllegalArgumentException("vendor must not be null!");

		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		ObjectIDUtil.assertValidIDString(orderIDPrefix, "orderIDPrefix");

		if (orderID < 0)
			throw new IllegalArgumentException("orderID < 0");

		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		this.vendor = vendor;
		this.customer = customer;
		this.customerGroup = customer.getDefaultCustomerGroup();
		this.organisationID = vendor.getOrganisationID();
		this.orderIDPrefix = orderIDPrefix;
		this.orderID = orderID;
		this.currency = currency;

		this.createDT = new Date();
		this.createUser = user;
		this.changeDT = new Date();
		this.changeUser = user;

		articles = new HashSet();
		offers = new HashSet();
		segments = new HashSet();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getOrderIDPrefix()
	{
		return orderIDPrefix;
	}
	/**
	 * @return Returns the orderID.
	 */
	public long getOrderID()
	{
		return orderID;
	}

	public static String getPrimaryKey(String organisationID, String orderIDPrefix, long orderID)
	{
		return organisationID +'/' + orderIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(orderID);
	}
	public String getPrimaryKey()
	{
		return organisationID + '/' + orderIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(orderID);
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
	/**
	 * @return Returns the vendor.
	 */
	public OrganisationLegalEntity getVendor()
	{
		return vendor;
	}
	/**
	 * @return Returns the customer.
	 */
	public LegalEntity getCustomer()
	{
		return customer;
	}

	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = (AnchorID) JDOHelper.getObjectId(vendor);

		return vendorID;
	}

	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = (AnchorID) JDOHelper.getObjectId(customer);

		return customerID;
	}

	/**
	 * @param customer The customer to set.
	 */
	public void setCustomer(LegalEntity customer)
	{
		if (!customerChangeable)
			throw new IllegalStateException("Customer cannot be changed anymore!");

		this.customer = customer;
	}
	/**
	 * After the first <tt>Offer</tt> has been finalized, the <tt>Order</tt>'s <tt>customer</tt>
	 * and <tt>customerGroup</tt> cannot be changed anymore.
	 *
	 * @return Returns <tt>true</tt>, if the <tt>customerGroup</tt> or the
	 *		<tt>customer</tt> are still changeable. Otherwise, null.
	 */
	public boolean isCustomerChangeable()
	{
		return customerChangeable;
	}
	/**
	 * @return Returns the customerGroup.
	 */
	public CustomerGroup getCustomerGroup()
	{
		return customerGroup;
	}
	/**
	 * @param customerGroup The customerGroup to set.
	 */
	public void setCustomerGroup(CustomerGroup customerGroup)
	{
		if (!customerChangeable)
			throw new IllegalStateException("CustomerGroup cannot be changed anymore!");

		this.customerGroup = customerGroup;
	}
	/**
	 * @return Returns the changeDT.
	 */
	public Date getChangeDT()
	{
		return changeDT;
	}
	/**
	 * @return Returns the changeUser.
	 */
	public User getChangeUser()
	{
		return changeUser;
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
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<Article> _articles = null;

	public Collection<Article> getArticles()
	{
		if (_articles == null)
			_articles = Collections.unmodifiableSet(articles);

		return _articles;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<Offer> _offers = null;

	/**
	 * @return Returns the offers.
	 */
	public Collection<Offer> getOffers()
	{
		if (_offers == null)
			_offers = Collections.unmodifiableSet(offers);

		return _offers;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set _segments = null;

	/**
	 * @return Returns the segments.
	 */
	@SuppressWarnings("unchecked")
	public Collection getSegments()
	{
		if (_segments == null)
			_segments = Collections.unmodifiableSet(segments);

		return _segments;
	}

	/**
	 * This method is called by {@link Trader} to add a newly created <tt>Segment</tt>.
	 */
	@SuppressWarnings("unchecked")
	protected void addSegment(Segment segment)
	{
		if (!getPrimaryKey().equals(segment.getOrder().getPrimaryKey()))
			throw new IllegalArgumentException("segment.order != this !!!");

		segments.add(segment);
	}

	/**
	 * Adds an article. This method is called by {@link Offer#addArticle(Article)}. You should
	 * never use this method directly in the server! It might be used, though in the client when
	 * working with a detached copy. In this case you MUST NEVER store this detached copy to the
	 * server again!
	 *
	 * @param article The <tt>Article</tt> to add.
	 *
	 * @see ArticleContainer#addArticle(Article)
	 */
	@SuppressWarnings("unchecked")
	public void addArticle(Article article)
	{
		articles.add(article);
	}

	public void removeArticle(Article article)
	{
		articles.remove(article.getPrimaryKey());
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Order is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public boolean isQuickSaleWorkOrder()
	{
		return quickSaleWorkOrder;
	}
	public void setQuickSaleWorkOrder(boolean quickSaleWorkOrder)
	{
		this.quickSaleWorkOrder = quickSaleWorkOrder;
	}

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _attached)
	{
		Order attached = (Order)_attached;
		Order detached = this;
		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof Order))
			return false;

		Order o = (Order) obj;

		return
				Utils.equals(this.organisationID, o.organisationID) && 
				Utils.equals(this.orderIDPrefix, o.orderIDPrefix) &&
				this.orderID == o.orderID;
	}

	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(this.organisationID) ^ 
				Utils.hashCode(this.orderIDPrefix) ^
				Utils.hashCode(this.orderID);
	}
}
