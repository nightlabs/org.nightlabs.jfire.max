package org.nightlabs.jfire.trade.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerTransferPolicy;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerTransferPolicyID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class EndCustomerTransferPolicyDAO
extends BaseJDOObjectDAO<EndCustomerTransferPolicyID, EndCustomerTransferPolicy>
{
	private static volatile EndCustomerTransferPolicyDAO _sharedInstance;

	public static EndCustomerTransferPolicyDAO sharedInstance()
	{
		if (_sharedInstance == null) {
			synchronized (EndCustomerTransferPolicyDAO.class) {
				if (_sharedInstance == null)
					_sharedInstance = new EndCustomerTransferPolicyDAO();
			}
		}
		return _sharedInstance;
	}

	@Override
	protected Collection<EndCustomerTransferPolicy> retrieveJDOObjects(
			Set<EndCustomerTransferPolicyID> endCustomerTransferPolicyIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		TradeManager tm = tradeManager;
		if (tm == null)
			tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());

		return CollectionUtil.castCollection(
				tm.getEndCustomerTransferPolicies(endCustomerTransferPolicyIDs, fetchGroups, maxFetchDepth)
		);
	}

	public EndCustomerTransferPolicy getEndCustomerTransferPolicy(
			EndCustomerTransferPolicyID endCustomerTransferPolicyID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObject(null, endCustomerTransferPolicyID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<EndCustomerTransferPolicy> getEndCustomerTransferPolicies(
			Collection<EndCustomerTransferPolicyID> endCustomerTransferPolicyIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, endCustomerTransferPolicyIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private TradeManager tradeManager = null;

	public synchronized List<EndCustomerTransferPolicy> getEndCustomerTransferPolicies(
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		tradeManager = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
		try {
			Collection<EndCustomerTransferPolicyID> endCustomerTransferPolicyIDs = CollectionUtil.castCollection(
					tradeManager.getEndCustomerTransferPolicyIDs()
			);
			return getJDOObjects(null, endCustomerTransferPolicyIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} finally {
			tradeManager = null;
		}
	}
}
