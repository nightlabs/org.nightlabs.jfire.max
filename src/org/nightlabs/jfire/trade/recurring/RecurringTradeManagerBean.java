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

package org.nightlabs.jfire.trade.recurring;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.version.MalformedVersionException;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/RecurringTradeManager"
 *					 jndi-name="jfire/ejb/JFireTrade/RecurringTradeManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class RecurringTradeManagerBean
extends BaseSessionBeanImpl
implements RecurringTradeManagerRemote, RecurringTradeManagerLocal
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RecurringTradeManagerBean.class);

	private static final String RECURRING_OFFER_PROCESS_DEFINITION_NAME_VENDOR = "RecurringOffer.Vendor";

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws IOException, MalformedVersionException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);

			// get Extents for the new classes is necessary because otherwise
			// DataNucleus creates queries on non-existing tables when no instance of those
			// classes was persisted yet and the tables were not lazily created
			pm.getExtent(RecurringOrder.class);
			pm.getExtent(RecurringOffer.class);
			pm.getExtent(RecurredOffer.class);

			// persist process definitions
			ProcessDefinitionID processDefinitionOfferVendorID = ProcessDefinitionID.create(Organisation.DEV_ORGANISATION_ID, RECURRING_OFFER_PROCESS_DEFINITION_NAME_VENDOR);
			ProcessDefinition processDefinitionOfferVendor;
			try {
				processDefinitionOfferVendor = (ProcessDefinition) pm.getObjectById(processDefinitionOfferVendorID);
			} catch (JDOObjectNotFoundException e) {
				processDefinitionOfferVendor = recurringTrader.storeProcessDefinitionRecurringOffer(TradeSide.vendor, ProcessDefinitionAssignment.class.getResource("recurring/offer/vendor/"));
				pm.makePersistent(new ProcessDefinitionAssignment(RecurringOffer.class, TradeSide.vendor, processDefinitionOfferVendor));
			}
			// TODO: Need process definitions for customer side later

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
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#processRecurringOfferTimed(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void processRecurringOfferTimed(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Task task = (Task) pm.getObjectById(taskID);
			RecurringOffer recurringOffer =  (RecurringOffer) task.getParam();
			logger.info("Have recurringOffer " + JDOHelper.getObjectId(recurringOffer));
			// Create the recurred Offer
			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
			recurringTrader.processRecurringOffer(recurringOffer);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * Creates a new Recurring Purchase order. This method is intended to be called by a user (not another
//	 * organisation).
//	 *
//	 * @param vendorID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
//	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
//	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
//	 * @param fetchGroups What fields should be detached.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
//	 * @ejb.transaction type="Required"
//	 **/
//	public RecurringOrder createPurchaseRecurringOrder(
//			AnchorID vendorID, String orderIDPrefix, CurrencyID currencyID,
//			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
//	{
//		if (vendorID == null)
//			throw new IllegalArgumentException("vendorID must not be null!");
//
//		if (currencyID == null)
//			throw new IllegalArgumentException("currencyID must not be null!");
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
//			Trader trader = Trader.getTrader(pm);
//			pm.getExtent(Currency.class);
//			Currency currency = (Currency)pm.getObjectById(currencyID);
//
//			pm.getExtent(LegalEntity.class);
//			LegalEntity vendor = (LegalEntity) pm.getObjectById(vendorID);
//
//			RecurringOrder recurringOrder = recurringTrader.createRecurringOrder(vendor, trader.getMandator(),orderIDPrefix, currency);
//
//			if (segmentTypeIDs != null)
//				createSegments(pm, trader, recurringOrder, segmentTypeIDs);
//
//			// TODO JPOX WORKAROUND BEGIN
//			// JDOHelper.getObjectId(order.getSegments().iterator().next()) returns null => trying to evict cache and reload a clean object
//			{
//				OrderID orderID = (OrderID) JDOHelper.getObjectId(recurringOrder);
//				pm.evictAll();
//				recurringOrder = (RecurringOrder) pm.getObjectById(orderID);
//			}
//			// TODO JPOX WORKAROUND END
//
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			return pm.detachCopy(recurringOrder);
//		} finally {
//			pm.close();
//		}
//	}



	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#createSaleRecurringOrder(org.nightlabs.jfire.transfer.id.AnchorID, java.lang.String, org.nightlabs.jfire.accounting.id.CurrencyID, org.nightlabs.jfire.trade.id.SegmentTypeID[], java.lang.String[], int)
	 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	public RecurringOrder createSaleRecurringOrder(
			AnchorID customerID, String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		if (customerID == null)
			throw new IllegalArgumentException("customerID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			RecurringTrader recurringTrader = RecurringTrader.getRecurringTrader(pm);
			Trader trader = Trader.getTrader(pm);

			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(currencyID);

			pm.getExtent(LegalEntity.class);
			LegalEntity customer = (LegalEntity) pm.getObjectById(customerID);

			RecurringOrder order = recurringTrader.createRecurringOrder(trader.getMandator(), customer, orderIDPrefix, currency);

			if (segmentTypeIDs != null)
				createSegments(pm, trader, order, segmentTypeIDs);
			{
				OrderID orderID = (OrderID) JDOHelper.getObjectId(order);
				pm.evictAll();
				order = (RecurringOrder) pm.getObjectById(orderID);
			}

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#createRecurringOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public RecurringOffer createRecurringOffer(OrderID orderID, String offerIDPrefix, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			RecurringTrader trader =  RecurringTrader.getRecurringTrader(pm);
			pm.getExtent(RecurringOrder.class);
			RecurringOrder order = (RecurringOrder) pm.getObjectById(orderID);
			RecurringOffer offer = trader.createRecurringOffer(User.getUser(pm, getPrincipal()), order, offerIDPrefix);

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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#storeRecurringOfferConfiguration(org.nightlabs.jfire.trade.recurring.RecurringOfferConfiguration, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	public RecurringOfferConfiguration storeRecurringOfferConfiguration(RecurringOfferConfiguration configuration, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// The store storeRecurringOfferConfiguration of RecurringTrader
			// obtains its own PersistenceManager, that will also be the one here
			return RecurringTrader.getRecurringTrader(pm).storeRecurringOfferConfiguration(configuration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#getRecurringOrders(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	public List<RecurringOrder> getRecurringOrders(Set<OrderID> orderIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, orderIDs, RecurringOrder.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.recurring.RecurringTradeManagerRemote#getRecurringOffers(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	public List<RecurringOffer> getRecurringOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, offerIDs, RecurringOffer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

}
