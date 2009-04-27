package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.CustomerGroupMapping;
import org.nightlabs.jfire.trade.TradeManagerRemote;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.CustomerGroupMappingID;
import org.nightlabs.progress.ProgressMonitor;

public class CustomerGroupMappingDAO
extends BaseJDOObjectDAO<CustomerGroupMappingID, CustomerGroupMapping>
{
	private static CustomerGroupMappingDAO _sharedInstance = null;

	public static synchronized CustomerGroupMappingDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new CustomerGroupMappingDAO();

		return _sharedInstance;
	}

	protected CustomerGroupMappingDAO() { }

	@Override
	protected Collection<CustomerGroupMapping> retrieveJDOObjects(
			Set<CustomerGroupMappingID> customerGroupMappingIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		TradeManagerRemote tm = tradeManager;
		if (tm == null)
			tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return tm.getCustomerGroupMappings(customerGroupMappingIDs, fetchGroups, maxFetchDepth);
	}

	private TradeManagerRemote tradeManager;

	public synchronized List<CustomerGroupMapping> getCustomerGroupMappings(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			tradeManager = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<CustomerGroupMappingID> customerGroupMappingIDs = tradeManager.getCustomerGroupMappingIDs();
				return getJDOObjects(null, customerGroupMappingIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				tradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<CustomerGroupMapping> getCustomerGroupMappings(Set<CustomerGroupMappingID> customerGroupMappingIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, customerGroupMappingIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public CustomerGroupMapping createCustomerGroupMapping(CustomerGroupID localCustomerGroupID, CustomerGroupID partnerCustomerGroupID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			CustomerGroupMapping cgm = tm.createCustomerGroupMapping(localCustomerGroupID, partnerCustomerGroupID, get, fetchGroups, maxFetchDepth);

			if (cgm != null)
				Cache.sharedInstance().put(null, cgm, fetchGroups, maxFetchDepth);

			return cgm;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
