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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
import org.nightlabs.jfire.trade.deliverydate.ArticleContainerDeliveryDateDTO;
import org.nightlabs.jfire.trade.deliverydate.ArticleDeliveryDateCarrier;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleEndCustomerHistoryItemID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.CustomerGroupMappingID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.prop.OfferStruct;
import org.nightlabs.jfire.trade.prop.OrderStruct;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class TradeManagerBean
extends BaseSessionBeanImpl
implements TradeManagerRemote, TradeManagerLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TradeManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createQuickSaleWorkOrder(org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String, org.nightlabs.jfire.accounting.id.CurrencyID, org.nightlabs.jfire.trade.id.SegmentTypeID[])
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public OrderID createQuickSaleWorkOrder(AnchorID customerID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createPurchaseOrder(org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String, org.nightlabs.jfire.accounting.id.CurrencyID, org.nightlabs.jfire.trade.id.SegmentTypeID[], java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Order createPurchaseOrder(
			AnchorID vendorID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	//	throws ModuleException
	{
		if (vendorID == null)
			throw new IllegalArgumentException("vendorID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		PersistenceManager pm = createPersistenceManager();
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


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createSaleOrder(org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String, org.nightlabs.jfire.accounting.id.CurrencyID, org.nightlabs.jfire.trade.id.SegmentTypeID[], java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Order createSaleOrder(
			AnchorID customerID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	//	throws ModuleException
	{
		if (customerID == null)
			throw new IllegalArgumentException("customerID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createCrossTradeSegments(org.nightlabs.jfire.trade.id.OrderID, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Collection<Segment> createCrossTradeSegments(OrderID orderID, Collection<SegmentTypeID> segmentTypeIDs)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createCrossTradeOrder(java.lang.String, java.lang.String, org.nightlabs.jfire.trade.id.CustomerGroupID, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Order createCrossTradeOrder(String orderIDPrefix, String currencyID, CustomerGroupID customerGroupID, Collection<SegmentTypeID> segmentTypeIDs)
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createCrossTradeOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer createCrossTradeOffer(OrderID orderID, String offerIDPrefix)
	throws ModuleException
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createCrossTradeReverseOffer(java.util.Collection, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer createCrossTradeReverseOffer(Collection<ArticleID> reversedArticleIDs, String offerIDPrefix)
	throws ModuleException
	{
		if (!getPrincipal().userIsOrganisation())
			throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

		PersistenceManager pm = createPersistenceManager();
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
					Offer.FETCH_GROUP_ARTICLES,
//					Article.FETCH_GROUP_PRICE, Article.FETCH_GROUP_PRODUCT, Article.FETCH_GROUP_PRODUCT_TYPE,
//					Article.FETCH_GROUP_REVERSED_ARTICLE, Article.FETCH_GROUP_REVERSING_ARTICLE,
//					Article.FETCH_GROUP_ORDER
					FetchGroupsTrade.FETCH_GROUP_ARTICLE_CROSS_TRADE_REPLICATION,
			});

			return pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer createOffer(OrderID orderID, String offerIDPrefix, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getNonFinalizedNonEndedOffers(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	public List<Offer> getNonFinalizedNonEndedOffers(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#reverseArticles(org.nightlabs.jfire.trade.id.OfferID, java.util.Collection, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.reverseProductType")
	public Collection<Article> reverseArticles(OfferID offerID, Collection<ArticleID> reversedArticleIDs, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createReverseOffer(java.util.Collection, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer createReverseOffer(
			Collection<ArticleID> reversedArticleIDs, String offerIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getAnonymousLegalEntity(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public LegalEntity getAnonymousLegalEntity(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOrganisationLegalEntity(java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public OrganisationLegalEntity getOrganisationLegalEntity(
			String organisationID, boolean throwExceptionIfNotExistent, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOrderIDs(java.lang.Class, boolean, org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, org.nightlabs.jfire.transfer.id.AnchorID, long, long)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	public List<OrderID> getOrderIDs(Class<? extends Order> orderClass, boolean subclasses, AnchorID vendorID, AnchorID customerID, AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return new ArrayList<OrderID>(Order.getOrderIDs(pm, orderClass, subclasses,vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOrders(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	public List<Order> getOrders(Set<OrderID> orderIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, orderIDs, Order.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#storePersonAsLegalEntity(org.nightlabs.jfire.person.Person, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public LegalEntity storePersonAsLegalEntity(Person person, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getLegalEntityForPerson(org.nightlabs.jfire.prop.id.PropertySetID, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public LegalEntity getLegalEntityForPerson(PropertySetID personID, String[] fetchGroups, int maxFetchDepth) {
		if (personID == null)
			throw new IllegalArgumentException("personID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		Person person = (Person) pm.getObjectById(personID);
		LegalEntity legalEntity = LegalEntity.getLegalEntity(pm, person);

		if (legalEntity == null)
			return null;

		pm.getFetchPlan().setGroups(fetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

		return pm.detachCopy(legalEntity);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#storeLegalEntity(org.nightlabs.jfire.trade.LegalEntity, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public LegalEntity storeLegalEntity(LegalEntity legalEntity, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (legalEntity.isAnonymous())
				throw new IllegalArgumentException("Attempt to change anonymous LegalEntity");
			return NLJDOHelper.storeJDO(pm, legalEntity, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOrder(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	public Order getOrder(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getLegalEntities(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public Collection<LegalEntity> getLegalEntities(Set<AnchorID> anchorIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOffer(org.nightlabs.jfire.trade.id.OfferID, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	public Offer getOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOffers(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	@SuppressWarnings("unchecked")
	public List<Offer> getOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, offerIDs, Offer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getArticles(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"org.nightlabs.jfire.trade.queryOrders", "org.nightlabs.jfire.trade.queryOffers", "org.nightlabs.jfire.store.queryDeliveryNotes", "org.nightlabs.jfire.accounting.queryInvoices"})
	public Collection<Article> getArticles(Collection<ArticleID> articleIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createSegment(org.nightlabs.jfire.trade.id.OrderID, org.nightlabs.jfire.trade.id.SegmentTypeID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Segment createSegment(
			OrderID orderID, SegmentTypeID segmentTypeID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#deleteArticles(java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Deprecated
	public Collection<Article> deleteArticles(Collection<ArticleID> articleIDs, boolean validate, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		return deleteArticles(articleIDs, get, fetchGroups, maxFetchDepth);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#deleteArticles(java.util.Collection, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Collection<Article> deleteArticles(Collection<ArticleID> articleIDs, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#releaseArticles(java.util.Collection, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Collection<Article> releaseArticles(Collection<ArticleID> articleIDs, boolean synchronously, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#signalOffer(org.nightlabs.jfire.trade.id.OfferID, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public void signalOffer(OfferID offerID, String jbpmTransitionName)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Trader.getTrader(pm).signalOffer(offerID, jbpmTransitionName);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws IOException, MalformedVersionException, TimePatternFormatException
	{
		PersistenceManager pm = createPersistenceManager();
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
					JFireTradeEAR.MODULE_NAME, "0.9.7.0.beta", "0.9.7.0.beta");
			pm.makePersistent(moduleMetaData);

			// Initalise standard property set structures for articleContainers
			OfferStruct.getOfferStructLocal(pm);
			OrderStruct.getOrderStructLocal(pm);

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
						TradeManagerLocal.class, "releaseExpiredUnfinalizedOffers"
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
						TradeManagerLocal.class, "releaseExpiredFinalizedOffers"
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#releaseExpiredUnfinalizedOffers(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void releaseExpiredUnfinalizedOffers(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#releaseExpiredFinalizedOffers(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void releaseExpiredFinalizedOffers(TaskID taskID)
	{
		PersistenceManager pm = createPersistenceManager();
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


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#assignCustomer(org.nightlabs.jfire.trade.id.OrderID, org.nightlabs.jfire.transfer.id.AnchorID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public Order assignCustomer(OrderID orderID, AnchorID customerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#setOfferExpiry(org.nightlabs.jfire.trade.id.OfferID, java.util.Date, boolean, java.util.Date, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer setOfferExpiry(
			OfferID offerID,
			Date expiryTimestampUnfinalized, boolean expiryTimestampUnfinalizedAutoManaged,
			Date expiryTimestampFinalized, boolean expiryTimestampFinalizedAutoManaged,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getCustomerGroupMappingIDs()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Set<CustomerGroupMappingID> getCustomerGroupMappingIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(CustomerGroupMapping.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<CustomerGroupMappingID>((Collection<? extends CustomerGroupMappingID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getCustomerGroupMappings(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Collection<CustomerGroupMapping> getCustomerGroupMappings(Collection<CustomerGroupMappingID> customerGroupMappingIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, customerGroupMappingIDs, CustomerGroupMapping.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createCustomerGroupMapping(org.nightlabs.jfire.trade.id.CustomerGroupID, org.nightlabs.jfire.trade.id.CustomerGroupID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editCustomerGroupMapping")
	public CustomerGroupMapping createCustomerGroupMapping(CustomerGroupID localCustomerGroupID, CustomerGroupID partnerCustomerGroupID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getCustomerGroupIDs(java.lang.String, boolean)
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Set<CustomerGroupID> getCustomerGroupIDs(String organisationID, boolean inverse)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getCustomerGroups(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Collection<CustomerGroup> getCustomerGroups(Collection<CustomerGroupID> customerGroupIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, customerGroupIDs, CustomerGroup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#storeCustomerGroup(org.nightlabs.jfire.trade.CustomerGroup, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editCustomerGroup")
	public CustomerGroup storeCustomerGroup(CustomerGroup customerGroup, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, customerGroup, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getProcessDefinitionIDs(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(ProcessDefinitionAssignment.getProcessDefinitions(pm, statableClassName));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getArticleContainerIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed({"org.nightlabs.jfire.trade.queryOrders", "org.nightlabs.jfire.trade.queryOffers", "org.nightlabs.jfire.store.queryDeliveryNotes", "org.nightlabs.jfire.accounting.queryInvoices"})
	public <R extends ArticleContainer> Set<ArticleContainerID> getArticleContainerIDs(
			QueryCollection<? extends AbstractArticleContainerQuery> queries)
	{
			Collection<R> articleContainers = getArticleContainers(queries);
			return NLJDOHelper.getObjectIDSet(articleContainers);
	}

	private <R extends ArticleContainer> Collection<R> getArticleContainers(
			QueryCollection<? extends AbstractArticleContainerQuery> queries)
	{
		if (queries == null)
			return null;
		if (! ArticleContainer.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = createPersistenceManager();
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
			return articleContainers;
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOfferIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
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

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getOrderIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
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

		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#assignTariff(java.util.Set, org.nightlabs.jfire.accounting.id.TariffID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Collection<Article> assignTariff(Set<ArticleID> articleIDs, TariffID tariffID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#crossOrganisationRegistrationCallback(org.nightlabs.jfire.crossorganisationregistrationinit.Context)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void crossOrganisationRegistrationCallback(Context context) throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createReverseOfferForProduct(org.nightlabs.jfire.store.id.ProductID, boolean, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public Offer createReverseOfferForProduct(
			ProductID productID,
			boolean completeOffer,
			boolean get,
			String[] fetchGroups,
			int maxFetchDepth
	)
	throws ReverseProductException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getProcessDefinitionIDs(java.lang.String, org.nightlabs.jfire.trade.TradeSide)
	 */
	@RolesAllowed("_Guest_")
	public Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName, TradeSide tradeSide)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(ProcessDefinitionAssignment.getProcessDefinitions(pm, statableClassName, tradeSide));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#createReservation(org.nightlabs.jfire.trade.id.OrderID, org.nightlabs.jfire.transfer.id.AnchorID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public void createReservation(OrderID orderID, AnchorID customerID)
	{
		// TODO: check/add rightsmanagement
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getMyProductTypePermissionFlagSetIDs(java.util.Set)
	 */
	@RolesAllowed("_Guest_")
	public Set<ProductTypePermissionFlagSetID> getMyProductTypePermissionFlagSetIDs(Set<ProductTypeID> productTypeIDs)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getProductTypePermissionFlagSets(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<ProductTypePermissionFlagSet> getProductTypePermissionFlagSets(
			Collection<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs,
			String[] fetchGroups,
			int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getEndCustomerReplicationPolicyIDs()
	 */
	@RolesAllowed("_Guest_")
	public Set<EndCustomerReplicationPolicyID> getEndCustomerReplicationPolicyIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(EndCustomerReplicationPolicy.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<? extends EndCustomerReplicationPolicyID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<EndCustomerReplicationPolicyID>(c);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#getEndCustomerReplicationPolicies(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<EndCustomerReplicationPolicy> getEndCustomerReplicationPolicies(
			Collection<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs,
			String[] fetchGroups,
			int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, endCustomerReplicationPolicyIDs, EndCustomerReplicationPolicy.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public void assignEndCustomer(AnchorID endCustomerID, Set<ArticleID> assignArticleIDs)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			LegalEntity endCustomer = (LegalEntity) pm.getObjectById(endCustomerID);
			Set<Article> articles = NLJDOHelper.getObjectSet(pm, assignArticleIDs, Article.class, QueryOption.throwExceptionOnMissingObject);
			for (Article article : articles) {
				article.setEndCustomer(endCustomer);
			}
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public void storeEndCustomer(LegalEntity endCustomer, Set<ArticleID> assignArticleIDs)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			LegalEntity attachedEndCustomer = EndCustomerReplicationPolicy.attachLegalEntity(pm, endCustomer);
			Set<Article> articles = NLJDOHelper.getObjectSet(pm, assignArticleIDs, Article.class, QueryOption.throwExceptionOnMissingObject);
			for (Article article : articles) {
				article.setEndCustomer(attachedEndCustomer);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.TradeManagerRemote#assignDeliveryDate(java.util.Map, org.nightlabs.jfire.trade.DeliveryDateMode, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Override
	public Collection<Article> assignDeliveryDate(
			Collection<ArticleDeliveryDateCarrier> articleDeliveryDateCarriers,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Collection<Article> articles = new ArrayList<Article>(articleDeliveryDateCarriers.size());
			for (ArticleDeliveryDateCarrier articleDeliveryDateCarrier : articleDeliveryDateCarriers) {
				Article article = (Article) pm.getObjectById(articleDeliveryDateCarrier.getArticleID());
				switch (articleDeliveryDateCarrier.getMode()) {
					case OFFER:
						article.setDeliveryDateOffer(articleDeliveryDateCarrier.getDeliveryDate());
						break;
					case DELIVERY_NOTE:
						article.setDeliveryDateDeliveryNote(articleDeliveryDateCarrier.getDeliveryDate());
						break;
				}
				articles.add(article);
			}
			return NLJDOHelper.storeJDOCollection(pm, articles, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}


	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleEndCustomerHistoryItemID> getArticleEndCustomerHistoryItemIDs(ArticleID articleID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Article article = (Article) pm.getObjectById(articleID);
			Collection<? extends ArticleEndCustomerHistoryItem> items = ArticleEndCustomerHistoryItem.getArticleContainerEndCustomerHistoryItems(pm, article);
			return NLJDOHelper.getObjectIDList(items);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleEndCustomerHistoryItem> getArticleEndCustomerHistoryItems(Collection<ArticleEndCustomerHistoryItemID> articleEndCustomerHistoryItemIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, articleEndCustomerHistoryItemIDs, ArticleEndCustomerHistoryItem.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed({"org.nightlabs.jfire.trade.queryOrders", "org.nightlabs.jfire.trade.queryOffers", "org.nightlabs.jfire.store.queryDeliveryNotes", "org.nightlabs.jfire.accounting.queryInvoices"})
	@Override
	public Collection<ArticleContainerDeliveryDateDTO> getArticleContainerDeliveryDateDTOs(
			QueryCollection<? extends AbstractArticleContainerQuery> queries)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// ask result for each contained query separatly andmerge afterwards,
			// because otherwise only result which matches all criteria is returned
			// which is e.g. not wanted in case you want results for offers and deliverynotes
			Collection<ArticleContainerDeliveryDateDTO> dtos = new ArrayList<ArticleContainerDeliveryDateDTO>();
			Collection<ArticleContainer> articleContainers = new HashSet<ArticleContainer>();
			for (AbstractArticleContainerQuery query : queries) {
				QueryCollection<AbstractArticleContainerQuery> qc =
					new QueryCollection<AbstractArticleContainerQuery>(ArticleContainer.class);
				qc.add(query);
				Collection<ArticleContainer> ac = getArticleContainers(qc);
				articleContainers.addAll(ac);
			}

			for (ArticleContainer ac : articleContainers) {
				ArticleContainerDeliveryDateDTO dto = new ArticleContainerDeliveryDateDTO();
				dto.setArticleContainerID((ArticleContainerID) JDOHelper.getObjectId(ac));
				Map<ArticleID, Article> articleID2Article = new HashMap<ArticleID, Article>();
				for (Article article : ac.getArticles()) {
					ArticleID articleID = (ArticleID) JDOHelper.getObjectId(article);
					// we put null here as value to minimize transport traffic, data will be filled up in ArticleContainerDAO
					// to increase chance that needed data is already cached
					articleID2Article.put(articleID, null);
				}
				dto.setArticleID2Article(articleID2Article);
				dtos.add(dto);
			}
			return dtos;
		} finally {
			pm.close();
		}
	}
}

