package org.nightlabs.jfire.trade.recurring.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.recurring.RecurringOrder;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManager;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManagerUtil;
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
		
	
}
