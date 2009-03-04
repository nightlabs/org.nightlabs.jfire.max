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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffOrderConfigModule;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.idgenerator.IDNamespaceDefault;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.ReceptionNote;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.store.reverse.AlreadyReversedArticleReverseProductError;
import org.nightlabs.jfire.store.reverse.IReverseProductError;
import org.nightlabs.jfire.store.reverse.NoArticlesFoundReverseProductError;
import org.nightlabs.jfire.store.reverse.OfferNotAcceptedReverseProductError;
import org.nightlabs.jfire.store.reverse.ReverseProductException;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.config.LegalEntityViewConfigModule;
import org.nightlabs.jfire.trade.config.OfferConfigModule;
import org.nightlabs.jfire.trade.config.TradePrintingConfigModule;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.CustomerGroupMappingID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.query.AbstractArticleContainerQuery;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.version.MalformedVersionException;


/**
 * @ejb.bean name="jfire/ejb/JFireTrade/TradeManager"
 *					 jndi-name="jfire/ejb/JFireTrade/TradeManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class TradeManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TradeManagerBean.class);

	////////////////////// EJB "constuctor" ////////////////////////////

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	// //// begin EJB stuff ////
	@Override
	public void setSessionContext(SessionContext ctx)
	throws EJBException, RemoteException
	{
		super.setSessionContext(ctx);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	// //// end EJB stuff ////

	/**
	 * This method only creates a new <code>Order</code>, if there is no unlocked, empty
	 * quick-sale-order existing. If it can re-use a previously created <code>Order</code>,
	 * it is locked and its ID returned.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	public OrderID createQuickSaleWorkOrder(AnchorID customerID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT,
					Order.FETCH_GROUP_OFFERS
			});

			Trader trader = Trader.getTrader(pm);
			OrganisationLegalEntity vendor = trader.getMandator();
			AnchorID vendorID = (AnchorID) JDOHelper.getObjectId(vendor);
			User user = User.getUser(pm, getPrincipal());
			UserID userID = (UserID) JDOHelper.getObjectId(user);

			pm.getExtent(EditLockTypeOrder.class);

			orderIDPrefix = trader.getOrderIDPrefix(user, orderIDPrefix);

			OrderID orderID = null;
			// TODO we need to take the SegmentTypes into account - either we ensure they exist after taking just any Order returned now or we reduce the candidates.
			List<OrderID> orderIDs = Order.getQuickSaleWorkOrderIDCandidates(pm, vendorID, customerID, userID, orderIDPrefix, currencyID);
			for (OrderID oID : orderIDs) {
				if (EditLock.getEditLockCount(pm, oID) == 0) {
					orderID = oID;
					break;
				}
			}

			if (orderID == null) {
				LegalEntity customer = (LegalEntity) pm.getObjectById(customerID);
				Currency currency = (Currency) pm.getObjectById(currencyID);
				Order order = trader.createOrder(vendor, customer, orderIDPrefix, currency);
				if (segmentTypeIDs != null)
					createSegments(pm, trader, order, segmentTypeIDs);

				order.setQuickSaleWorkOrder(true);
				orderID = (OrderID) JDOHelper.getObjectId(order);
			}

			EditLock.acquireEditLock(pm, userID, getSessionID(), EditLockTypeOrder.EDIT_LOCK_TYPE_ID, orderID, "QuickSaleWorkOrder"); // TODO nice description

			return orderID;
		} finally {
			pm.close();
		}
	}


	/**
	 * Creates a new Purchase order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param vendorID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	public Order createPurchaseOrder(
			AnchorID vendorID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	//	throws ModuleException
	{
		if (vendorID == null)
			throw new IllegalArgumentException("vendorID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);

			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(currencyID);

			pm.getExtent(LegalEntity.class);
			LegalEntity vendor = (LegalEntity) pm.getObjectById(vendorID);

			Order order = trader.createOrder(vendor, trader.getMandator(),orderIDPrefix, currency);

			if (segmentTypeIDs != null)
				createSegments(pm, trader, order, segmentTypeIDs);

			// TODO JPOX WORKAROUND BEGIN
			// JDOHelper.getObjectId(order.getSegments().iterator().next()) returns null => trying to evict cache and reload a clean object
			{
				OrderID orderID = (OrderID) JDOHelper.getObjectId(order);
				pm.evictAll();
				order = (Order) pm.getObjectById(orderID);
			}
			// TODO JPOX WORKAROUND END

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}


	/**
	 * Creates a new Sale order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param customerID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	public Order createSaleOrder(
			AnchorID customerID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	//	throws ModuleException
	{
		if (customerID == null)
			throw new IllegalArgumentException("customerID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);

			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(currencyID);

			pm.getExtent(LegalEntity.class);
			LegalEntity customer = (LegalEntity) pm.getObjectById(customerID);

			Order order = trader.createOrder(trader.getMandator(), customer, orderIDPrefix, currency);

			if (segmentTypeIDs != null)
				createSegments(pm, trader, order, segmentTypeIDs);

			// TODO JPOX WORKAROUND BEGIN
			// JDOHelper.getObjectId(order.getSegments().iterator().next()) returns null => trying to evict cache and reload a clean object
			{
				OrderID orderID = (OrderID) JDOHelper.getObjectId(order);
				pm.evictAll();
				order = (Order) pm.getObjectById(orderID);
			}
			// TODO JPOX WORKAROUND END

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}


	private static void createSegments(PersistenceManager pm, Trader trader, Order order, SegmentTypeID[] segmentTypeIDs)
	{
		pm.getExtent(SegmentType.class);
		for (int i = 0; i < segmentTypeIDs.length; ++i) {
			SegmentTypeID segmentTypeID = segmentTypeIDs[i];
			SegmentType segmentType = null;
			if (segmentTypeID != null) {
				segmentType = (SegmentType) pm.getObjectById(segmentTypeID);
			}
			trader.createSegment(order, segmentType);
		}
	}

	/**
	 * This method is called, if the cross-trade-{@link Order} already exists (from a previous call to {@link #createCrossTradeOrder(String, String, CustomerGroupID, Set)}),
	 * but additional Segments are required.
	 *
	 * @param orderID The ID of the {@link Order} for which the new {@link Segment}s shall be created.
	 * @param segmentTypeIDs For each {@link SegmentTypeID} in this {@link Collection}, a new instance of {@link Segment} will be created and returned. If you pass
	 *		a {@link List} here with multiple references to the same {@link SegmentTypeID}, multiple instances of <code>Segment</code> will be created for this
	 *		same {@link SegmentType}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 */
	public Collection<Segment> createCrossTradeSegments(OrderID orderID, Collection<SegmentTypeID> segmentTypeIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);
			List<SegmentType> segmentTypes = NLJDOHelper.getObjectList(pm, segmentTypeIDs, SegmentType.class);
			Collection<Segment> segments = Trader.getTrader(pm).createSegments(order, segmentTypes);

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT, Segment.FETCH_GROUP_ORDER, Segment.FETCH_GROUP_SEGMENT_TYPE
			});
			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);

			return pm.detachCopyAll(segments);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new order. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the customer for the new Order.
	 *
	 * @param customerGroupID Either <code>null</code> (then the default will be used) or an ID of a {@link CustomerGroup} which is allowed to the customer.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 */
	public Order createCrossTradeOrder(String orderIDPrefix, String currencyID, CustomerGroupID customerGroupID, Collection<SegmentTypeID> segmentTypeIDs)
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(CurrencyID.create(currencyID), true);

			// cut off the User.USER_ID_PREFIX_TYPE_ORGANISATION
			String customerOrganisationID = getPrincipal().getUserID().substring(User.USER_ID_PREFIX_TYPE_ORGANISATION.length());
			OrganisationLegalEntity customer = trader.getOrganisationLegalEntity(customerOrganisationID);

			CustomerGroup customerGroup = null;
			if (customerGroupID != null) {
				pm.getExtent(CustomerGroup.class);
				customerGroup = (CustomerGroup) pm.getObjectById(customerGroupID);
				if (!customer.containsCustomerGroup(customerGroup))
					throw new IllegalArgumentException("The given CustomerGroupID cannot be assigned, because it is not available to the customer! " + customerGroupID + " customer="+customer.getPrimaryKey());
			}

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT, Order.FETCH_GROUP_CHANGE_USER, Order.FETCH_GROUP_CREATE_USER,
					Order.FETCH_GROUP_CURRENCY, Order.FETCH_GROUP_CUSTOMER, Order.FETCH_GROUP_CUSTOMER_GROUP,
					Order.FETCH_GROUP_SEGMENTS, Order.FETCH_GROUP_VENDOR, Segment.FETCH_GROUP_ORDER, Segment.FETCH_GROUP_SEGMENT_TYPE
			});
			//			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);

			Order order = trader.createOrder(trader.getMandator(), customer, orderIDPrefix, currency);

			if (customerGroup != null)
				order.setCustomerGroup(customerGroup);

			if (segmentTypeIDs != null) {
				List<SegmentType> segmentTypes = NLJDOHelper.getObjectList(pm, segmentTypeIDs, SegmentType.class);
				Trader.getTrader(pm).createSegments(order, segmentTypes);
			}

			return pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new Offer within a given Order. This method is only usable, if the user (principal)
	 * is an organisation.
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	public Offer createCrossTradeOffer(OrderID orderID, String offerIDPrefix)
	throws ModuleException
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Order.class);
			Offer offer = trader.createOffer(
					User.getUser(pm, getPrincipal()), (Order) pm.getObjectById(orderID, true), offerIDPrefix);

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT, Offer.FETCH_GROUP_CREATE_USER, Offer.FETCH_GROUP_ORDER,
					Offer.FETCH_GROUP_CURRENCY, Statable.FETCH_GROUP_STATE, Statable.FETCH_GROUP_STATES,
					State.FETCH_GROUP_STATABLE, State.FETCH_GROUP_STATE_DEFINITION, State.FETCH_GROUP_USER
			});

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	public Offer createCrossTradeReverseOffer(Collection<ArticleID> reversedArticleIDs, String offerIDPrefix)
	throws ModuleException
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Order.class);

			// TODO check whether for all requested Articles, the user is allowed to perform reverse!
			Set<Article> reversedArticles = NLJDOHelper.getObjectSet(pm, reversedArticleIDs, Article.class);
			Offer offer = trader.createReverseOffer(user, reversedArticles, offerIDPrefix);

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT, Offer.FETCH_GROUP_CREATE_USER, Offer.FETCH_GROUP_ORDER,
					Offer.FETCH_GROUP_CURRENCY, Statable.FETCH_GROUP_STATE, Statable.FETCH_GROUP_STATES, Offer.FETCH_GROUP_PRICE,
					Offer.FETCH_GROUP_CREATE_USER,
					State.FETCH_GROUP_STATABLE, State.FETCH_GROUP_STATE_DEFINITION, State.FETCH_GROUP_USER,
					Offer.FETCH_GROUP_ARTICLES, Article.FETCH_GROUP_PRICE, Article.FETCH_GROUP_PRODUCT, Article.FETCH_GROUP_PRODUCT_TYPE,
					Article.FETCH_GROUP_REVERSED_ARTICLE, Article.FETCH_GROUP_REVERSING_ARTICLE,
					Article.FETCH_GROUP_ORDER
			});

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}


	/**
	 * Creates a new Offer within a given Order.
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	public Offer createOffer(OrderID orderID, String offerIDPrefix, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);
			Offer offer = trader.createOffer(User.getUser(pm, getPrincipal()), order, offerIDPrefix);

			// At the moment, we add the first default segment we find in the order automatically to the offer.
			// This should be changed later, since not every offer requires the default segment... Marco.
			for (Segment segment : order.getSegments()) {
				if (JDOHelper.getObjectId(segment.getSegmentType()).equals(SegmentType.DEFAULT_SEGMENT_TYPE_ID)) {
					offer.addSegment(segment);
					break;
				}
			}

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

	//	/**
	//	* Rejects the specified {@link Offer}.
	//	*
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="TradeManager-write"
	//	* @ejb.transaction type="Required"
	//	**/
	//	public Offer rejectOffer(OfferID offerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	//	throws ModuleException
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	pm.getExtent(Offer.class);

	//	if (get) {
	//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
	//	if (fetchGroups != null)
	//	pm.getFetchPlan().setGroups(fetchGroups);
	//	}

	//	Offer offer = (Offer) pm.getObjectById(offerID);
	//	Trader.getTrader(pm).rejectOffer(User.getUser(pm, getPrincipal()), offer);

	//	if (get)
	//	return pm.detachCopy(offer);
	//	else
	//	return null;
	//	} finally {
	//	pm.close();
	//	}
	//	}

	/**
	 * @param orderID The orderID defining the Order for which to find all non-finalized offers.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @ejb.transaction type="Required"
	 **/
	public List<Offer> getNonFinalizedNonEndedOffers(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (List<Offer>) pm.detachCopyAll(Offer.getNonFinalizedNonEndedOffers(pm, order));
		} finally {
			pm.close();
		}
	}

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "reversing"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * the specified {@link Offer} reversing all the specified <code>Article</code>s.
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 *		Note, that there are no special requirements for this <code>Offer</code> (it can either be created by
	 *		{@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} or by
	 *		{@link TradeManager#createReverseOffer(java.util.Collection, boolean, java.lang.String[])} (or other create-methods)).
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.reverseProductType"
	 * @ejb.transaction type="Required"
	 **/
	public Collection<Article> reverseArticles(OfferID offerID, Collection<ArticleID> reversedArticleIDs, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);
			Offer offer = (Offer) pm.getObjectById(offerID);

			pm.getExtent(Article.class);
			Order order = null;
			List<Article> reversedArticles = new ArrayList<Article>(reversedArticleIDs.size());
			Set<ProductType> productTypes = new HashSet<ProductType>();
			for (ArticleID articleID : reversedArticleIDs) {
				Article article = (Article) pm.getObjectById(articleID);
				if (order == null)
					order = article.getOrder();
				else if (!order.equals(article.getOrder()))
					throw new IllegalArgumentException("Not all Articles are in the same Order!");

				productTypes.add(article.getProductType());
				reversedArticles.add(article);
			}
			if (order == null)
				throw new IllegalArgumentException("Collection reversedArticleIDs must not be empty!");

			if (!order.equals(offer.getOrder()))
				throw new IllegalArgumentException("Specified offer is not in the same order as the specified articles!");

			// check for all requested articles' product types, whether 'org.nightlabs.jfire.trade.reverseProductType' is allowed!
			for (ProductType productType : productTypes) {
				Authority.resolveSecuringAuthority(pm, productType.getProductTypeLocal(), ResolveSecuringAuthorityStrategy.allow).assertContainsRoleRef(getPrincipal(), RoleConstants.reverseProductType);
			}

			User user = User.getUser(pm, getPrincipal());

			Collection<Article> reversingArticles = Trader.getTrader(pm).reverseArticles(user, offer, reversedArticles);

			offer.validate();

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(reversingArticles);
		} finally {
			pm.close();
		}
	}

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "inversed"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * a newly created {@link Offer} reversing all the specified <code>Article</code>s.
	 * <p>
	 * This method is a shortcut for
	 * {@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} combined
	 * with
	 * {@link TradeManager#reverseArticle(org.nightlabs.jfire.trade.id.OfferID, org.nightlabs.jfire.trade.id.ArticleID, boolean, java.lang.String[])}
	 * or
	 * {@link TradeManager#reverseArticles(org.nightlabs.jfire.trade.id.OfferID, java.util.Collection, boolean, java.lang.String[])}.
	 * </p>
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	public Offer createReverseOffer(
			Collection<ArticleID> reversedArticleIDs, String offerIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<Article> reversedArticles = NLJDOHelper.getObjectSet(pm, reversedArticleIDs, Article.class);
			User user = User.getUser(pm, getPrincipal());
			Trader trader = Trader.getTrader(pm);

			// check for all requested articles' product types, whether 'org.nightlabs.jfire.trade.reverseProductType' is allowed!
			Set<ProductType> productTypes = new HashSet<ProductType>();
			for (Article reversedArticle : reversedArticles) {
				productTypes.add(reversedArticle.getProductType());
			}
			for (ProductType productType : productTypes) {
				Authority.resolveSecuringAuthority(
						pm,
						productType.getProductTypeLocal(),
						ResolveSecuringAuthorityStrategy.organisation // We must use "organisation" here, because the EJB-level check does not yet check for the role! Marco.
				).assertContainsRoleRef(getPrincipal(), RoleConstants.reverseProductType);
			}

			// create the reversing offer
			Offer offer = trader.createReverseOffer(user, reversedArticles, offerIDPrefix);

			offer.validate();

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method delegates to
	 * {@link LegalEntity#getAnonymousLegalEntity(PersistenceManager)}.
	 * <p>
	 * It's OK if everyone can read the anonymous business partner, because that's not confidential.
	 * Therefore, this method can be called by every authenticated user.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public LegalEntity getAnonymousLegalEntity(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			LegalEntity le = LegalEntity.getAnonymousLegalEntity(pm);
			return pm.detachCopy(le);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method delegates to
	 * {@link OrganisationLegalEntity#getOrganisationLegalEntity(PersistenceManager, String)}.
	 * <p>
	 * At the moment, this method can be called by every authenticated user. We believe that the revealed
	 * information is not so extremely confidential - the same
	 * information (address, VAT number, phone number and the like) can ususally be found on a web site, too.
	 * We might change this later, and filter some of the {@link Person}'s data blocks as well as maybe
	 * some information of the {@link LegalEntity}.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public OrganisationLegalEntity getOrganisationLegalEntity(
			String organisationID, boolean throwExceptionIfNotExistent, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			OrganisationLegalEntity ole = OrganisationLegalEntity.getOrganisationLegalEntity(
					pm, organisationID);

			return pm.detachCopy(ole);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Order</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Order}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<OrderID> getOrderIDs(Class<? extends Order> orderClass, boolean subclasses, AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<OrderID>(Order.getOrderIDs(pm, orderClass, subclasses,vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<Order> getOrders(Set<OrderID> orderIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, orderIDs, Order.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Stores the given Person to a LegalEntity. If no LegalEntity with the right
	 * AnchorID is found a new one will be created and made persistent.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException} on an
	 * attempt to change the person of an anonymous {@link LegalEntity}.
	 * </p>
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @param person The person to be set to the LegalEntity
	 * @param get If true the created LegalEntity will be returned else null
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public LegalEntity storePersonAsLegalEntity(Person person, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Trader trader = Trader.getTrader(pm);
			LegalEntity aLegalEntity = LegalEntity.getLegalEntity(pm, person);

			if (aLegalEntity != null && aLegalEntity.isAnonymous())
				person = aLegalEntity.getPerson();
			else
				person = pm.makePersistent(person);

			LegalEntity legalEntity = trader.setPersonToLegalEntity(person, true);
			if (get)
				return pm.detachCopy(legalEntity);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the {@link LegalEntity} for the given {@link Person} or <code>null</code> if none could be found.
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @param personID The object-id of the {@link Person} for which a {@link LegalEntity} is to be returned.
	 * @return the {@link LegalEntity} for the given {@link Person} or <code>null</code> if none could be found.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports"
	 */
	public LegalEntity getLegalEntityForPerson(PropertySetID personID, String[] fetchGroups, int maxFetchDepth) {
		if (personID == null)
			throw new IllegalArgumentException("personID must not be null!");

		PersistenceManager pm = getPersistenceManager();
		Person person = (Person) pm.getObjectById(personID);
		LegalEntity legalEntity = LegalEntity.getLegalEntity(pm, person);

		if (legalEntity == null)
			return null;

		pm.getFetchPlan().setGroups(fetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

		return pm.detachCopy(legalEntity);
	}

	/**
	 * Stores the given LegalEntity.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException} on an
	 * attempt to change the person of an anonymous {@link LegalEntity}.
	 * </p>
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 * @param legalEntity The LegalEntity to be stored
	 * @param get Whether the stored instance or null should be returned.
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 * @return The stored LegalEntity or null
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public LegalEntity storeLegalEntity(LegalEntity legalEntity, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (legalEntity.isAnonymous())
				throw new IllegalArgumentException("Attempt to change anonymous LegalEntity");
			return NLJDOHelper.storeJDO(pm, legalEntity, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @ejb.transaction type="Required"
	 */
	public Order getOrder(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Order) pm.detachCopy(pm.getObjectById(orderID));
		} finally {
			pm.close();
		}
	}

	//	/**
	//	* <p>
	//	* TODO https://www.jfire.org/modules/bugs/view.php?id=896
	//	* </p>
	//	*
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="_Guest_"
	//	* @ejb.transaction type="Required"
	//	*/
	//	public LegalEntity getLegalEntity(AnchorID anchorID, String[] fetchGroups, int maxFetchDepth)
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
	//	if (fetchGroups != null)
	//	pm.getFetchPlan().setGroups(fetchGroups);

	//	return (LegalEntity) pm.detachCopy(pm.getObjectById(anchorID));
	//	} finally {
	//	pm.close();
	//	}
	//	}

	//	/**
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="_Guest_"
	//	* @ejb.transaction type="Required"
	//	*/
	//	public Collection<LegalEntity> getLegalEntities(Object[] leAnchorIDs, String[] fetchGroups, int maxFetchDepth)
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	Collection<LegalEntity> les = new LinkedList<LegalEntity>();
	//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
	//	if (fetchGroups != null)
	//	pm.getFetchPlan().setGroups(fetchGroups);

	//	for (int i = 0; i < leAnchorIDs.length; i++) {
	//	if (!(leAnchorIDs[i] instanceof AnchorID))
	//	throw new IllegalArgumentException("leAnchorIDs["+i+" is not of type AnchorID");

	//	les.add((LegalEntity) pm.getObjectById(leAnchorIDs[i]));
	//	}

	//	long time = System.currentTimeMillis();
	//	Collection<LegalEntity> result = pm.detachCopyAll(les);
	//	time = System.currentTimeMillis() - time;
	//	logger.debug("Detach of "+result.size()+" LegalEntities took "+((double)time / (double)1000));
	//	return result;
	//	}
	//	finally {
	//	pm.close();
	//	}
	//	}

	/**
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<LegalEntity> getLegalEntities(Set<AnchorID> anchorIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<LegalEntity> les = new LinkedList<LegalEntity>();
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			for (AnchorID anchorID : anchorIDs) {
				les.add((LegalEntity) pm.getObjectById(anchorID));
			}

			long time = System.currentTimeMillis();
			Collection<LegalEntity> result = pm.detachCopyAll(les);
			time = System.currentTimeMillis() - time;
			logger.debug("Detach of "+result.size()+" LegalEntities took "+((double)time / (double)1000));
			return result;
		}
		finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Offer getOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Offer.class);
			return (Offer) pm.detachCopy(pm.getObjectById(offerID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<Offer> getOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, offerIDs, Offer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param articleIDs Instances of {@link ArticleID}.
	 * @return Returns instances of {@link Article}
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders, org.nightlabs.jfire.trade.queryOffers, org.nightlabs.jfire.store.queryDeliveryNotes, org.nightlabs.jfire.accounting.queryInvoices"
	 * @ejb.transaction type="Required"
	 */
	public Collection<Article> getArticles(Collection<ArticleID> articleIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {

			// TODO WORKAROUND experimental: Maybe this helps to get no outdated data anymore?! Marco.
			//			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			//			try {
			//			return NLJDOHelper.getDetachedObjectList(pm, articleIDs, Article.class, fetchGroups, maxFetchDepth);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<Article> articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);

			if (logger.isTraceEnabled()) {
				logger.trace("getArticles: got " + articles.size() + " articles from JDO:");
				for (Article article : articles) {
					logger.trace("getArticles: * " + article);
					logger.trace("getArticles:   - article.allocated=" + article.isAllocated());
					logger.trace("getArticles:   - article.allocationPending=" + article.isAllocationPending());
					logger.trace("getArticles:   - article.releasePending=" + article.isReleasePending());
				}
			}

			////			TODO WORKAROUND for DataNucleus bug: Without this, I get always version 2 of a newly created TicArticle - even though, in the datastore, there's already version 4, 5 or even higher.
			//			pm.refreshAll(articles);

			//			if (logger.isTraceEnabled()) {
			//			logger.trace("getArticles: refreshed " + articles.size() + ":");
			//			for (Article article : articles) {
			//			logger.trace("getArticles: * " + article);
			//			logger.trace("getArticles:   - article.allocated=" + article.isAllocated());
			//			logger.trace("getArticles:   - article.allocationPending=" + article.isAllocationPending());
			//			logger.trace("getArticles:   - article.releasePending=" + article.isReleasePending());
			//			}
			//			}

			articles = pm.detachCopyAll(articles);
			return articles;
			//			} finally {
			//			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			//			}
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new <tt>Segment</tt> within the given <tt>Order</tt>
	 * for the given <tt>SegmentType</tt>. Note, that you can create
	 * many <tt>Segment</tt>s with the same <tt>SegmentType</tt>.
	 *
	 * @param orderID The ID of the {@link Order} in which to create the new <tt>Segment</tt>.
	 * @param segmentTypeID The ID of the {@link SegmentType} of which a <tt>Segment</tt>
	 * "instance" will be created. This may be <tt>null</tt>. If undefined, the default
	 * segment type will be used.
	 * @param fetchGroups A <tt>String</tt> array defining what fields to detach.
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	public Segment createSegment(
			OrderID orderID, SegmentTypeID segmentTypeID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);

			SegmentType segmentType = null;
			if (segmentTypeID != null) {
				pm.getExtent(SegmentType.class);
				segmentType = (SegmentType) pm.getObjectById(segmentTypeID);
			}

			Trader trader = Trader.getTrader(pm);
			Segment segment = trader.createSegment(order, segmentType);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(segment);
		} finally {
			pm.close();
		}
	}

	/**
	 * @return <code>null</code> if <code>get == false</code>, otherwise those {@link Article}s that were <b>not</b> yet removed, because
	 *		they are released asynchronously first.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 *
	 * @deprecated use {@link #deleteArticles(Collection, boolean, String[], int)} instead.
	 */
	@Deprecated
	public Collection<Article> deleteArticles(Collection<ArticleID> articleIDs, boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		return deleteArticles(articleIDs, get, fetchGroups, maxFetchDepth);
	}

	/**
	 * @return <code>null</code> if <code>get == false</code>, otherwise those {@link Article}s that were <b>not</b> yet removed, because
	 *		they are released asynchronously first.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	public Collection<Article> deleteArticles(Collection<ArticleID> articleIDs, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<Article> articles;
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
				pm.refreshAll(articles);
			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}

			Collection<Article> nonAllocatedArticles = new HashSet<Article>(articleIDs.size());
			Set<Offer> offers = new HashSet<Offer>();
			Set<Article> allocatedArticles = new HashSet<Article>(articleIDs.size());
			for (Article article : articles) {
				offers.add(article.getOffer());

				if (article.isAllocated() || article.isAllocationPending())
					allocatedArticles.add(article);
				else
					nonAllocatedArticles.add(article);
			}

			Trader trader = Trader.getTrader(pm);

			if (!allocatedArticles.isEmpty())
				trader.releaseArticles(User.getUser(pm, getPrincipal()), allocatedArticles, false, true);

			if (!nonAllocatedArticles.isEmpty())
				trader.deleteArticles(User.getUser(pm, getPrincipal()), nonAllocatedArticles);

			for (Offer offer : offers)
				trader.validateOffer(offer);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(allocatedArticles);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	public Collection<Article> releaseArticles(Collection<ArticleID> articleIDs, boolean synchronously, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<Article> articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);

			Trader trader = Trader.getTrader(pm);
			trader.releaseArticles(User.getUser(pm, getPrincipal()), articles, synchronously, false);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}

	/**
	 *
	 * signal a given Jbpm transition to the offer
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	public void signalOffer(OfferID offerID, String jbpmTransitionName)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader.getTrader(pm).signalOffer(offerID, jbpmTransitionName);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws IOException, MalformedVersionException, TimePatternFormatException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// WORKAROUND JPOX Bug to avoid problems with creating workflows as State.statable is defined as interface and has subclassed implementations
			// http://www.jpox.org/servlet/jira/browse/NUCCORE-93
			pm.getExtent(Order.class);
			pm.getExtent(Offer.class);
			pm.getExtent(Invoice.class);
			pm.getExtent(ReceptionNote.class);
			pm.getExtent(DeliveryNote.class);
			pm.getExtent(RecurringOffer.class);
			pm.getExtent(RecurredOffer.class);
			pm.getExtent(RecurringOrder.class);

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireTradeEAR.MODULE_NAME);

			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireTrade-ConfigModules started...");

			// version is {major}.{minor}.{release}.{patchlevel}.{suffix}
			moduleMetaData = new ModuleMetaData(
					JFireTradeEAR.MODULE_NAME, "0.9.5.0.beta", "0.9.5.0.beta");
			pm.makePersistent(moduleMetaData);

			ConfigSetup userConfigSetup = ConfigSetup.getConfigSetup(
					pm,
					getOrganisationID(),
					UserConfigSetup.CONFIG_SETUP_TYPE_USER
			);
			userConfigSetup.getConfigModuleClasses().add(LegalEntityViewConfigModule.class.getName());
			userConfigSetup.getConfigModuleClasses().add(TariffOrderConfigModule.class.getName());

			ConfigSetup workstationConfigSetup = ConfigSetup.getConfigSetup(
					pm,
					getOrganisationID(),
					WorkstationConfigSetup.CONFIG_SETUP_TYPE_WORKSTATION
			);
			workstationConfigSetup.getConfigModuleClasses().add(TradePrintingConfigModule.class.getName());
			workstationConfigSetup.getConfigModuleClasses().add(OfferConfigModule.class.getName());

			Trader trader = Trader.getTrader(pm);

			// ensure that the anonymous customer exists
			LegalEntity.getAnonymousLegalEntity(pm);

			// persist process definitions
			ProcessDefinition processDefinitionOfferCustomerLocal;
			processDefinitionOfferCustomerLocal = trader.storeProcessDefinitionOffer(TradeSide.customerLocal, ProcessDefinitionAssignment.class.getResource("offer/customer/local/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Offer.class, TradeSide.customerLocal, processDefinitionOfferCustomerLocal));

			ProcessDefinition processDefinitionOfferCustomerCrossOrg;
			processDefinitionOfferCustomerCrossOrg = trader.storeProcessDefinitionOffer(TradeSide.customerCrossOrganisation, ProcessDefinitionAssignment.class.getResource("offer/customer/crossorganisation/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Offer.class, TradeSide.customerCrossOrganisation, processDefinitionOfferCustomerCrossOrg));

			ProcessDefinition processDefinitionOfferVendor;
			processDefinitionOfferVendor = trader.storeProcessDefinitionOffer(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("offer/vendor/"));
			pm.makePersistent(new ProcessDefinitionAssignment(Offer.class, TradeSide.vendor, processDefinitionOfferVendor));


			// switch off the IDGenerator's caches for Order and Offer
			IDNamespaceDefault idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Order.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);

			idNamespaceDefault = IDNamespaceDefault.createIDNamespaceDefault(pm, getOrganisationID(), Offer.class);
			idNamespaceDefault.setCacheSizeServer(0);
			idNamespaceDefault.setCacheSizeClient(0);

			pm.makePersistent(new EditLockTypeOrder(EditLockTypeOrder.EDIT_LOCK_TYPE_ID));
			pm.makePersistent(new EditLockTypeOffer(EditLockTypeOffer.EDIT_LOCK_TYPE_ID));

			EditLockType productTypeEditLock = new EditLockType(JFireTradeEAR.EDIT_LOCK_TYPE_ID_PRODUCT_TYPE);
			productTypeEditLock = pm.makePersistent(productTypeEditLock);
			// TODO do sth. with productTypeEditLock or remove this variable (btw. after pm.makePersistent(...) it is crutial to continue work with the result of this method!)

			// WORKAROUND JPOX Bug to avoid java.util.ConcurrentModificationException in OfferRequirement.getOfferRequirement(OfferRequirement.java:86) at runtime
			pm.getExtent(OfferRequirement.class);


			// In case the JFireTrade module is deployed into a running server, there might already exist cooperations
			// with other organisations => need to create OrganisationLegalEntities - see #crossOrganisationRegistrationCallback(...)
			for (Iterator<Organisation> it = pm.getExtent(Organisation.class).iterator(); it.hasNext(); ) {
				initialisePartnerLegalEntity(pm, it.next().getOrganisationID());
			}

			{
				Task task = new Task(
						getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "releaseExpiredUnfinalizedOffers",
						User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
						TradeManagerHome.JNDI_NAME, "releaseExpiredUnfinalizedOffers"
				);
				task = pm.makePersistent(task);
				task.getTimePatternSet().createTimePattern("*", "*", "*", "*", "*/6", "0");

				task.getName().setText(Locale.ENGLISH.getLanguage(), "Release expired UNfinalized offers");
				task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task releases expired offers that are *not* finalized.");

				task.getName().setText(Locale.GERMAN.getLanguage(), "Freigabe unfinalisierter ausgelaufener Angebote");
				task.getDescription().setText(Locale.GERMAN.getLanguage(), "Dieser Task gibt Angebote frei, deren Verfallsdatum erreicht ist und die *nicht* finalisiert sind.");
				task.setEnabled(true);
			}

			{
				Task task = new Task(
						getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "releaseExpiredFinalizedOffers",
						User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
						TradeManagerHome.JNDI_NAME, "releaseExpiredFinalizedOffers"
				);
				task = pm.makePersistent(task);
				task.getTimePatternSet().createTimePattern("*", "*", "*", "*", "0", "0");

				task.getName().setText(Locale.ENGLISH.getLanguage(), "Release expired finalized offers");
				task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task releases expired offers that are finalized.");

				task.getName().setText(Locale.GERMAN.getLanguage(), "Freigabe finalisierter ausgelaufener Angebote");
				task.getDescription().setText(Locale.GERMAN.getLanguage(), "Dieser Task gibt Angebote frei, deren Verfallsdatum erreicht ist und die bereits finalisiert sind.");
				task.setEnabled(true);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void releaseExpiredUnfinalizedOffers(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = null;
			User user = null;
			Query q = pm.newQuery(Offer.class);
			q.declareVariables(Article.class.getName() + " allocatedArticle");
			q.setFilter(
					"this.order.vendor == :localOrganisationLegalEntity && " +
					"this.finalizeDT == null && " +
					"this.expiryTimestampUnfinalized < :now && " +
					"this.articles.contains(allocatedArticle) && " +
					"allocatedArticle.allocated"
			);
			OrganisationLegalEntity localOrganisationLegalEntity = OrganisationLegalEntity.getOrganisationLegalEntity(pm, getOrganisationID());
			Collection<?> c = (Collection<?>) q.execute(localOrganisationLegalEntity, new Date());
			for (Iterator<?> it = c.iterator(); it.hasNext();) {
				Offer offer = (Offer) it.next();

				if (trader == null)
					trader = Trader.getTrader(pm);

				if (user == null)
					user = User.getUser(pm, getPrincipal());

				Collection<? extends Article> offerArticles = offer.getArticles();
				Set<Article> allocatedArticles = new HashSet<Article>(offerArticles.size());
				for (Article article : offerArticles) {
					if (article.isAllocated())
						allocatedArticles.add(article);
				}

				if (allocatedArticles.isEmpty())
					logger.warn("releaseExpiredUnfinalizedOffers: Even though the Offer was found by the query it doesn't have allocated articles! " + offer);
				else
					trader.releaseArticles(user, allocatedArticles, true, false);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void releaseExpiredFinalizedOffers(TaskID taskID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			OrganisationLegalEntity localOrganisationLegalEntity = OrganisationLegalEntity.getOrganisationLegalEntity(pm, getOrganisationID());
			Query q = pm.newQuery(Offer.class);
			q.setFilter(
					"this.order.vendor == :localOrganisationLegalEntity && " +
					"this.finalizeDT != null && " +
					"this.offerLocal.acceptDT == null && " +
					"this.offerLocal.rejectDT == null && " +
					"!this.offerLocal.processEnded && " +
					"this.expiryTimestampFinalized < :now"
			);
			Collection<?> c = (Collection<?>) q.execute(localOrganisationLegalEntity, new Date());

			if (c.isEmpty())
				return; // nothing to do

			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
			try {

				for (Iterator<?> it = c.iterator(); it.hasNext();) {
					Offer offer = (Offer) it.next();

					ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(offer.getOfferLocal().getJbpmProcessInstanceId());
					processInstance.signal(JbpmConstantsOffer.Vendor.TRANSITION_NAME_EXPIRE);
				}

			} finally {
				jbpmContext.close();
			}
		} finally {
			pm.close();
		}
	}


	/**
	 * This method assigns a customer to an {@link Order}. This fails with
	 * an {@link IllegalStateException}, if the <code>Order</code> contains
	 * at least one finalized {@link Offer}.
	 *
	 * @param orderID The ID of the {@link Order} that shall be linked to another customer.
	 * @param customerID The ID of the {@link LegalEntity} which shall be the new customer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	public Order assignCustomer(OrderID orderID, AnchorID customerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Order.class);
			pm.getExtent(LegalEntity.class);

			Order order = (Order) pm.getObjectById(orderID);
			LegalEntity newCustomer = (LegalEntity) pm.getObjectById(customerID);

			Trader.getTrader(pm).assignCustomer(order, newCustomer);

			if (!get)
				return null;

			return pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	public Offer setOfferExpiry(
			OfferID offerID,
			Date expiryTimestampUnfinalized, boolean expiryTimestampUnfinalizedAutoManaged,
			Date expiryTimestampFinalized, boolean expiryTimestampFinalizedAutoManaged,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Offer offer = (Offer) pm.getObjectById(offerID);
			if (!offer.isFinalized()) {
				offer.setExpiryTimestampUnfinalizedAutoManaged(expiryTimestampUnfinalizedAutoManaged);
				offer.setExpiryTimestampUnfinalized(expiryTimestampUnfinalized);
			}
			if (!offer.getOfferLocal().isAccepted()) {
				offer.setExpiryTimestampFinalizedAutoManaged(expiryTimestampFinalizedAutoManaged);
				offer.setExpiryTimestampFinalized(expiryTimestampFinalized);
			}

			Trader.getTrader(pm).setOfferExpiry(offer);

			if (!get)
				return null;

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all mappings between customer-groups. These mappings are necessary for cross-organisation-trade.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any confidential data.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<CustomerGroupMappingID> getCustomerGroupMappingIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(CustomerGroupMapping.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<CustomerGroupMappingID>((Collection<? extends CustomerGroupMappingID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the mappings between customer-groups specified by their object-ids.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any highly confidential data.
	 * We will add filtering of CustomerGroups later, though, because this information is slightly confidential.
	 * </p>
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<CustomerGroupMapping> getCustomerGroupMappings(Collection<CustomerGroupMappingID> customerGroupMappingIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, customerGroupMappingIDs, CustomerGroupMapping.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editCustomerGroupMapping"
	 */
	public CustomerGroupMapping createCustomerGroupMapping(CustomerGroupID localCustomerGroupID, CustomerGroupID partnerCustomerGroupID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			CustomerGroupMapping tm = CustomerGroupMapping.create(pm, localCustomerGroupID, partnerCustomerGroupID);
			if (!get)
				return null;

			return pm.detachCopy(tm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the object-ids of all customer-groups matching a certain <code>organisationID</code> or all, if
	 * the specified <code>organisationID</code> is <code>null</code>.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any confidential data.
	 * </p>
	 *
	 * @param organisationID <code>null</code> in order to get all customerGroups (no filtering). non-<code>null</code> to filter by <code>organisationID</code>.
	 * @param inverse This applies only if <code>organisationID != null</code>. If <code>true</code>, it will return all {@link CustomerGroupID}s where the <code>organisationID</code>
	 *		is NOT the one passed as parameter <code>organisationID</code>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<CustomerGroupID> getCustomerGroupIDs(String organisationID, boolean inverse)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(CustomerGroup.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (organisationID != null)
				q.setFilter("this.organisationID " + (inverse ? "!=" : "==") + " :organisationID");

			return new HashSet<CustomerGroupID>((Collection<? extends CustomerGroupID>) q.execute(organisationID));
		} finally {
			pm.close();
		}
	}

	/**
	 * Get customer-groups specified by their object-ids.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any highly confidential data.
	 * We will add filtering of CustomerGroups later, though, because this information is slightly confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<CustomerGroup> getCustomerGroups(Collection<CustomerGroupID> customerGroupIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, customerGroupIDs, CustomerGroup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editCustomerGroup"
	 */
	public CustomerGroup storeCustomerGroup(CustomerGroup customerGroup, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, customerGroup, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(ProcessDefinitionAssignment.getProcessDefinitions(pm, statableClassName));
		} finally {
			pm.close();
		}
	}

	/**
	 * @param queries the QueryCollection containing all queries that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders, org.nightlabs.jfire.trade.queryOffers, org.nightlabs.jfire.store.queryDeliveryNotes, org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public <R extends ArticleContainer> Set<ArticleContainerID> getArticleContainerIDs(
			QueryCollection<? extends AbstractArticleContainerQuery> queries)
			{
		if (queries == null)
			return null;
		if (! ArticleContainer.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<? extends AbstractArticleContainerQuery> decoratedCollection;

			// DO not add / apply generics to the instanceof check, the sun compiler doesn't like it
			// and stop compilation with an "Unconvertible types" error!
			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<? extends AbstractArticleContainerQuery>) queries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<AbstractArticleContainerQuery>(queries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<R> articleContainers = (Collection<R>) decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(articleContainers);
		} finally {
			pm.close();
		}
			}

	//	/**
	//	* @param articleContainerQueries Instances of {@link ArticleContainerQuery}
	//	* 		that shall be chained
	//	*		in order to retrieve the result. The result of one query is passed to the
	//	*		next one using the {@link JDOQuery#setCandidates(Collection)}.
	//	*
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="_Guest_"
	//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	//	*/
	//	public Set<ArticleContainerID> getArticleContainerIDsForQueries(Collection<ArticleContainerQuery> articleContainerQueries)
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	pm.getFetchPlan().setMaxFetchDepth(1);
	//	pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

	//	Collection<ArticleContainer> articleContainers = null;
	//	for (ArticleContainerQuery query : articleContainerQueries) {
	//	query.setPersistenceManager(pm);
	//	query.setCandidates(articleContainers);
	//	articleContainers = (Collection) query.getResult();
	//	}

	//	return NLJDOHelper.getObjectIDSet(articleContainers);
	//	} finally {
	//	pm.close();
	//	}
	//	}

	//	/**
	//	* @param articleContainerQueries Instances of {@link AbstractArticleContainerQuery}
	//	* 		that shall be chained
	//	*		in order to retrieve the result. The result of one query is passed to the
	//	*		next one using the {@link JDOQuery#setCandidates(Collection)}.
	//	*
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="_Guest_"
	//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	//	*/
	//	public Set<ArticleContainerID> getArticleContainerIDsForQuickSearchQueries(Collection<AbstractArticleContainerQuery> articleContainerQuickSearchQueries)
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	pm.getFetchPlan().setMaxFetchDepth(1);
	//	pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

	//	Collection<AbstractArticleContainerQuery> articleContainers = null;
	//	for (AbstractArticleContainerQuery query : articleContainerQuickSearchQueries) {
	//	query.setPersistenceManager(pm);
	//	query.setCandidates(articleContainers);
	//	articleContainers = (Collection) query.getResult();
	//	}

	//	return NLJDOHelper.getObjectIDSet(articleContainers);
	//	} finally {
	//	pm.close();
	//	}
	//	}

	//	/**
	//	* @ejb.interface-method
	//	* @ejb.permission role-name="_Guest_"
	//	* @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	//	*/
	//	public Set<InvoiceID> getInvoiceIDs(Collection<JDOQuery> queries)
	//	{
	//	PersistenceManager pm = getPersistenceManager();
	//	try {
	//	pm.getFetchPlan().setMaxFetchDepth(1);
	//	pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

	//	Collection<Invoice> invoices = null;
	//	for (JDOQuery query : queries) {
	//	query.setPersistenceManager(pm);
	//	query.setCandidates(invoices);
	//	invoices = (Collection) query.getResult();
	//	}

	//	return NLJDOHelper.getObjectIDSet(invoices);
	//	} finally {
	//	pm.close();
	//	}
	//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<OfferID> getOfferIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			throw new IllegalArgumentException("queries must not be null!");

		if (queries.isEmpty())
			throw new IllegalArgumentException("queries must not be empty!");

		if (! Offer.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries;

			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedQueries = (JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;
			}
			else
			{
				decoratedQueries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}

			decoratedQueries.setPersistenceManager(pm);
			Collection<? extends Offer> offers =
				(Collection<? extends Offer>) decoratedQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(offers);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public Set<OrderID> getOrderIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Order.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<AbstractJDOQuery> decoratedQueries;

			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedQueries = (JDOQueryCollectionDecorator<AbstractJDOQuery>) queries;
			}
			else
			{
				decoratedQueries = new JDOQueryCollectionDecorator<AbstractJDOQuery>(queries);
			}

			decoratedQueries.setPersistenceManager(pm);
			Collection<? extends Order> orders =
				(Collection<? extends Order>) decoratedQueries.executeQueries();

			return NLJDOHelper.getObjectIDSet(orders);
		} finally {
			pm.close();
		}
	}

	/**
	 * Assign a tariff to the specified articles.
	 *
	 * @param articleIDs the object-ids of the articles to be changed.
	 * @param tariffID the object-id of the tariff to be assigned.
	 * @param get <code>false</code> if no result is desired (this method will return <code>null</code>) or <code>true</code> to get the specified articles
	 * @param fetchGroups the fetch-groups in case the affected articles shall be detached and returned. This is ignored, if <code>get</code> is <code>false</code>.
	 * @param maxFetchDepth the maximum fetch-depth - ignored, if <code>get</code> is <code>false</code>.
	 * @return either <code>null</code>, if <code>get</code> is <code>false</code> or the articles identified by <code>articleIDs</code>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	public Collection<Article> assignTariff(Set<ArticleID> articleIDs, TariffID tariffID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Tariff tariff = (Tariff) pm.getObjectById(tariffID);
			Set<Article> articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);

			Trader.getTrader(pm).assignTariff(User.getUser(pm, getPrincipal()), articles, tariff);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void crossOrganisationRegistrationCallback(Context context) throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// I think it's not necessary to ask the other organisation for its OrganisationLegalEntity. We have the person already
			// here, thus we can simply create it.
			initialisePartnerLegalEntity(pm, context.getOtherOrganisationID());
			// Note, that this is done for all existing organisations in #initialise() as well, because the JFireTrade module
			// might be deployed, AFTER a JFire server is already running for a long time.
		} finally {
			pm.close();
		}
	}

	private static void initialisePartnerLegalEntity(PersistenceManager pm, String partnerOrganisationID)
	{
		OrganisationLegalEntity.getOrganisationLegalEntity(pm, partnerOrganisationID);
	}

	/**
	 * Returns the reversing {@link Offer} which defines the reverse for the {@link Product}
	 * of the given {@link ProductID}.
	 * If the Article where the {@link Product} with the given {@link ProductID} is referenced,
	 * can not be reversed, a {@link ReverseProductException} is thrown which contains {@link IReverseProductError}s
	 * which describe why not.
	 *
	 * @param productID the {@link ProductID} to get the reversing {@link Offer} for.
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	public Offer createReverseOfferForProduct(
			ProductID productID,
			boolean completeOffer,
			boolean get,
			String[] fetchGroups,
			int maxFetchDepth
	)
	throws ReverseProductException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Product product = null;
			try {
				product = (Product) pm.getObjectById(productID);
			} catch (JDOObjectNotFoundException x) {
				return null;
			}

			Authority.resolveSecuringAuthority(
					pm,
					product.getProductType().getProductTypeLocal(),
					ResolveSecuringAuthorityStrategy.organisation // We must use organisation-fallback, because the reverseProductType role is not checked on method level!
			).assertContainsRoleRef(
					getPrincipal(),
					RoleConstants.reverseProductType
			);

			ReverseProductException result = new ReverseProductException(productID);
			// get all articles for product
			Set<Article> articles = Article.getArticles(pm, product);
			if (articles == null || articles.isEmpty()) {
				// no article found
				String description = "There was not article found for the given product";
				IReverseProductError error = new NoArticlesFoundReverseProductError(description);
				//				result.addReverseProductResultError(error);
				result.setReverseProductError(error);
				throw result;
			}

			// collect all allocated articles
			Set<Article> allocatedArticles = new HashSet<Article>();
			for (Article article : articles) {
				if (article.isAllocated()) {
					allocatedArticles.add(article);
				}
			}

			String description = null;
			// check if more than 1 Article is allocated
			if (allocatedArticles.size() > 1) {
				if (allocatedArticles.size() == 2) {
					Article reversingArticle = null;
					Article reversedArticle = null;
					for (Article article : allocatedArticles) {
						if (article.isReversing()) {
							if (reversingArticle != null) {
								throw new IllegalStateException("There are 2 reversing articles existing");
							}
							reversingArticle = article;
						}
						if (article.isReversed()) {
							if (reversedArticle != null) {
								throw new IllegalStateException("There are 2 reversed articles existing");
							}
							reversedArticle = article;
						}
					}
					description = "There already exists an reversed article";
					AlreadyReversedArticleReverseProductError error = new AlreadyReversedArticleReverseProductError(description);
					error.setReversedArticleID((ArticleID) JDOHelper.getObjectId(reversedArticle));
					error.setReversingArticleID((ArticleID) JDOHelper.getObjectId(reversingArticle));
					//					result.addReverseProductResultError(error);
					result.setReverseProductError(error);
					throw result;
				}
				else {
					description = String.format("There are %s articles allocated and none is reversing", allocatedArticles.size());
					throw new IllegalStateException(description);
				}
			}

			// exactly one allocated article found
			Article article = allocatedArticles.iterator().next();
			articles.clear();
			articles.add(article);
			Offer offer = article.getOffer();

			Trader trader = Trader.getTrader(pm);
			if (!trader.getMandator().equals(offer.getVendor()))
				throw new UnsupportedOperationException("NYI");

			// check if offer is accepted from both sides
			if (!State.hasState(pm, article.getOfferID(), JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED)) {
				description = "The offer ist not accepted.";
				OfferNotAcceptedReverseProductError error = new OfferNotAcceptedReverseProductError(description);
				error.setOfferID(article.getOfferID());
				//				result.addReverseProductResultError(error);
				result.setReverseProductError(error);
				throw result;
			}
			else {
				// everything is ok, article can be reversed, so create reversing offer
				User user = User.getUser(pm, getPrincipal());
				// check if complete offer should be reversed
				if (completeOffer) {
					Collection<Article> offerArticles = offer.getArticles();
					Set<Article> reversableArticles = new HashSet<Article>();
					// remove all reversing articles from offer
					for (Article offerArticle : offerArticles) {
						if (!offerArticle.isReversing() && !offerArticle.isReversed()) {
							reversableArticles.add(offerArticle);
						}
					}
					articles = reversableArticles;
				}
				Offer reversingOffer = trader.createReverseOffer(user, articles, null);
				reversingOffer.validate();

				if (!get)
					return null;

				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return pm.detachCopy(reversingOffer);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName, TradeSide tradeSide)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(ProcessDefinitionAssignment.getProcessDefinitions(pm, statableClassName, tradeSide));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	public void createReservation(OrderID orderID, AnchorID customerID)
	{
		// TODO: check/add rightsmanagement
		PersistenceManager pm = getPersistenceManager();
		try {
			// check if reservation is tried to be done for anonymous customer
			LegalEntity anonymousLegalEntity = LegalEntity.getAnonymousLegalEntity(pm);
			AnchorID anonymousID = (AnchorID) JDOHelper.getObjectId(anonymousLegalEntity);
			if (customerID.equals(anonymousID)) {
				throw new IllegalArgumentException("Can not make an reservation for the anonymous customer!");
			}
			Order order = (Order) pm.getObjectById(orderID);
			LegalEntity customer = (LegalEntity) pm.getObjectById(customerID);
			Trader trader = Trader.getTrader(pm);
			trader.assignCustomer(order, customer);
			for (Offer offer : order.getOffers())
			{
				if (!offer.isFinalized()) {
					OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
					trader.signalOffer(offerID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_FINALIZE);
				}
			}
		}
		finally {
			pm.close();
		}
	}

	/**
	 * Get all {@link ProductTypePermissionFlagSet}s assigned to the current user and the specified ProductTypeIDs.
	 *
	 * @param productTypeIDs the {@link ProductTypeID}s of those {@link ProductType}s for which to obtain {@link ProductTypePermissionFlagSet}s.
	 * @return the object-ids of the found {@link ProductTypePermissionFlagSet}s.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<ProductTypePermissionFlagSetID> getMyProductTypePermissionFlagSetIDs(Set<ProductTypeID> productTypeIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());
			Collection<ProductType> productTypes = CollectionUtil.castCollection(pm.getObjectsById(productTypeIDs));
			Set<ProductTypePermissionFlagSetID> res = new HashSet<ProductTypePermissionFlagSetID>();
			for (ProductType productType : productTypes) {
				ProductTypePermissionFlagSet flagSet = ProductTypePermissionFlagSet.getProductTypePermissionFlagSet(pm, productType, user, false);
				if (flagSet != null) // might not exist
					res.add((ProductTypePermissionFlagSetID) JDOHelper.getObjectId(flagSet));
			}
			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * Get the {@link ProductTypePermissionFlagSet}s specified by their object-ids.
	 *
	 * @param productTypePermissionFlagSetIDs the object-ids of the {@link ProductTypePermissionFlagSet}s to retrieve.
	 * @param fetchGroups the JDO-fetch-groups to use for detaching.
	 * @param maxFetchDepth the maximum depth of the object-graphs to be detached.
	 * @return the {@link ProductTypePermissionFlagSet}s for the specified object-ids.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<ProductTypePermissionFlagSet> getProductTypePermissionFlagSets(
			Collection<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs,
			String[] fetchGroups,
			int maxFetchDepth
	)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			List<ProductTypePermissionFlagSet> productTypePermissionFlagSets = new ArrayList<ProductTypePermissionFlagSet>(productTypePermissionFlagSetIDs.size());
			UserID userID = UserID.create(getPrincipal());
			for (ProductTypePermissionFlagSetID productTypePermissionFlagSetID : productTypePermissionFlagSetIDs) {
				if (productTypePermissionFlagSetID == null)
					throw new IllegalArgumentException("productTypePermissionFlagSetIDs contains null element!");

				// We currently filter and return only those that are assigned to the currently
				// authenticated user. Maybe we'll change this and introduce an access right
				// (if granted, filtering would be deactivated).
				if (!userID.organisationID.equals(productTypePermissionFlagSetID.userOrganisationID) || !userID.userID.equals(productTypePermissionFlagSetID.userID))
					continue;

				productTypePermissionFlagSets.add(
						(ProductTypePermissionFlagSet) pm.getObjectById(productTypePermissionFlagSetID)
				);
			}

			return pm.detachCopyAll(productTypePermissionFlagSets);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<EndCustomerReplicationPolicyID> getEndCustomerReplicationPolicyIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(EndCustomerReplicationPolicy.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<? extends EndCustomerReplicationPolicyID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<EndCustomerReplicationPolicyID>(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<EndCustomerReplicationPolicy> getEndCustomerReplicationPolicies(
			Collection<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs,
			String[] fetchGroups,
			int maxFetchDepth
	)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, endCustomerReplicationPolicyIDs, EndCustomerReplicationPolicy.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeEndCustomer(LegalEntity endCustomer, Set<OrderID> assignOrderIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LegalEntity attachedEndCustomer = EndCustomerReplicationPolicy.attachLegalEntity(pm, endCustomer);
			Set<Order> orders = NLJDOHelper.getObjectSet(pm, assignOrderIDs, Order.class, QueryOption.throwExceptionOnMissingObject);
			for (Order order : orders) {
				order.setEndCustomer(attachedEndCustomer);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}

