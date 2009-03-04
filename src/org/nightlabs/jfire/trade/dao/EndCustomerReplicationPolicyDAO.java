package org.nightlabs.jfire.trade.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class EndCustomerReplicationPolicyDAO
extends BaseJDOObjectDAO<EndCustomerReplicationPolicyID, EndCustomerReplicationPolicy>
{
	private static volatile EndCustomerReplicationPolicyDAO _sharedInstance;

	public static EndCustomerReplicationPolicyDAO sharedInstance()
	{
		if (_sharedInstance == null) {
			synchronized (EndCustomerReplicationPolicyDAO.class) {
				if (_sharedInstance == null)
					_sharedInstance = new EndCustomerReplicationPolicyDAO();
			}
		}
		return _sharedInstance;
	}

	@Override
	protected Collection<EndCustomerReplicationPolicy> retrieveJDOObjects(
			Set<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		TradeManager tm = tradeManager;
		if (tm == null)
			tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());

		return CollectionUtil.castCollection(
				tm.getEndCustomerReplicationPolicies(endCustomerReplicationPolicyIDs, fetchGroups, maxFetchDepth)
		);
	}

	public EndCustomerReplicationPolicy getEndCustomerReplicationPolicy(
			EndCustomerReplicationPolicyID endCustomerReplicationPolicyID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObject(null, endCustomerReplicationPolicyID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<EndCustomerReplicationPolicy> getEndCustomerReplicationPolicies(
			Collection<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, endCustomerReplicationPolicyIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private TradeManager tradeManager = null;

	public synchronized List<EndCustomerReplicationPolicy> getEndCustomerReplicationPolicies(
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		tradeManager = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
		try {
			Collection<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs = CollectionUtil.castCollection(
					tradeManager.getEndCustomerReplicationPolicyIDs()
			);
			return getJDOObjects(null, endCustomerReplicationPolicyIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} finally {
			tradeManager = null;
		}
	}
}
