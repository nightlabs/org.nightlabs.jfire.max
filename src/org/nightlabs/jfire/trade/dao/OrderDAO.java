package org.nightlabs.jfire.trade.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * Accessor for {@link Order} instances.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class OrderDAO extends BaseJDOObjectDAO<OrderID, Order> {

	private static OrderDAO sharedInstance = null;

	/**
	 * @return The shared instance of {@link OrderDAO}.
	 */
	public static OrderDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (OrderDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new OrderDAO();
			}
		}
		return sharedInstance;
	}

	protected OrderDAO() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	protected Collection<Order> retrieveJDOObjects(Set<OrderID> orderIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {
		TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
		return tm.getOrders(orderIDs, fetchGroups, maxFetchDepth);
	}

	/**
	 * Get the detached {@link Order} for the given {@link OrderID} using the cache.
	 *
	 * @param orderID The {@link OrderID} to get the {@link Order} for.
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The detached {@link Order} for the given {@link OrderID}.
	 */
	public Order getOrder(OrderID orderID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, orderID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Obtain the detached {@link Order}s for the given {@link OrderID}s using the cache.
	 *
	 * @param orderIDs The {@link OrderID}s to get {@link Order}s for.
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The detached {@link Order}s for the given {@link OrderID}s.
	 */
	public List<Order> getOrders(Set<OrderID> orderIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, orderIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get the detached results of the given query-collection.
	 *
	 * @param queries The queries used to search for the orders.
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The {@link Order}s that match the given queries.
	 */
	public Collection<Order> getOrdersByQueries(
			QueryCollection<? extends AbstractJDOQuery> queries,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			Set<OrderID> orderIDs = CollectionUtil.castSet(tm.getOrderIDs(queries));

			return getJDOObjects(null, orderIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(
					"Cannot fetch Orders via Queries:" + e.getLocalizedMessage(), e); //$NON-NLS-1$
		}
	}

	/**
	 * Queries the OrderIDs for the given vendor and customer from the server and returns the {@link Order} instances
	 * using the Cache.
	 * <p>
	 * Note that this method will return only {@link Order}s of the class {@link Order} not including subclasses
	 * </p>
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param endCustomerID TODO
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The {@link Order}s (of the given class) for the given vendor and owner.
	 */
	public List<Order> getOrders(AnchorID vendorID, AnchorID customerID,
			AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getOrders(Order.class, false, vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Queries the OrderIDs for the given vendor and customer from the server and returns the {@link Order} instances
	 * using the Cache.
	 *
	 * @param orderClass The class of {@link Order}s to fetch.
	 * @param subclasses Whether to include subclasses of the given orderClass.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param endCustomerID TODO
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The {@link Order}s (of the given class) for the given vendor and owner.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public List<Order> getOrders(Class<? extends Order> orderClass, boolean subclasses, AnchorID vendorID, AnchorID customerID,
			AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			List<OrderID> orderIDList = tm.getOrderIDs(orderClass, subclasses, vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx);
			Set<OrderID> orderIDs = new HashSet<OrderID>(orderIDList);

			Map<OrderID, Order> orderMap = new HashMap<OrderID, Order>(orderIDs.size());
			for (Order order : getJDOObjects(null, orderIDs, fetchGroups, maxFetchDepth, monitor)) {
				orderMap.put((OrderID) JDOHelper.getObjectId(order), order);
			}

			List<Order> res = new ArrayList<Order>(orderIDList.size());
			for (OrderID orderID : orderIDList) {
				Order order = orderMap.get(orderID);
				if (order != null) {
					res.add(order);
				}
			}

			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
