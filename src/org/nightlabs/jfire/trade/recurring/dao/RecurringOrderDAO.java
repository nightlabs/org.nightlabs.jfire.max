package org.nightlabs.jfire.trade.recurring.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManager;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManagerUtil;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

public class RecurringOrderDAO 
extends BaseJDOObjectDAO<OrderID, RecurringOrder>
{
	private static  RecurringOrderDAO sharedInstance = null;

	public static RecurringOrderDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (RecurringOrderDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new RecurringOrderDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<RecurringOrder> retrieveJDOObjects(Set<OrderID> orderIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		
		RecurringTradeManager tm = RecurringTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return tm.getRecurringOrders(orderIDs, fetchGroups, maxFetchDepth);
	}
	
	public RecurringOrder getRecurringOrder(OrderID orderID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, orderID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<RecurringOrder> getRecurringOrders(Set<OrderID> orderIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, orderIDs, fetchGroups, maxFetchDepth, monitor);
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
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive). 
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The {@link Order}s (of the given class) for the given vendor and owner.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public List<RecurringOrder> getRecurringOrders(AnchorID vendorID, AnchorID customerID,
			long rangeBeginIdx, long rangeEndIdx, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getRecurringOrders(RecurringOrder.class, false, vendorID, customerID, rangeBeginIdx, rangeEndIdx, fetchGroups, maxFetchDepth, monitor);
	}
	
	
	/**
	 * Queries the OrderIDs for the given vendor and customer from the server and returns the {@link Order} instances
	 * using the Cache.
	 * 
	 * @param orderClass The class of {@link Order}s to fetch. 
	 * @param subclasses Whether to include subclasses of the given orderClass.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive). 
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @param fetchGroups The fetch-groups to detach the result with.
	 * @param maxFetchDepth The max fetch-depth to detach the result with.
	 * @param monitor The monitor to use to report progress.
	 * @return The {@link Order}s (of the given class) for the given vendor and owner.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public List<RecurringOrder> getRecurringOrders(Class<? extends Order> orderClass, boolean subclasses, AnchorID vendorID, AnchorID customerID,
			long rangeBeginIdx, long rangeEndIdx, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		try {
			TradeManager tm = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<OrderID> orderIDList = tm.getOrderIDs(orderClass, subclasses, vendorID, customerID, rangeBeginIdx, rangeEndIdx);
			Set<OrderID> orderIDs = new HashSet<OrderID>(orderIDList);

			Map<OrderID, RecurringOrder> orderMap = new HashMap<OrderID, RecurringOrder>(orderIDs.size());
			for (RecurringOrder order : getJDOObjects(null, orderIDs, fetchGroups, maxFetchDepth, monitor)) {
				orderMap.put((OrderID) JDOHelper.getObjectId(order), order);
			}

			List<RecurringOrder> res = new ArrayList<RecurringOrder>(orderIDList.size());
			for (OrderID orderID : orderIDList) {
				RecurringOrder order = orderMap.get(orderID);
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
