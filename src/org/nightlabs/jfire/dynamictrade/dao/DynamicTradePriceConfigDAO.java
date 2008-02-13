package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManager;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class DynamicTradePriceConfigDAO
extends BaseJDOObjectDAO<PriceConfigID, DynamicTradePriceConfig>
{
	private static DynamicTradePriceConfigDAO sharedInstance = null;

	public static DynamicTradePriceConfigDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DynamicTradePriceConfigDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DynamicTradePriceConfigDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	@Implement
	protected Collection<DynamicTradePriceConfig> retrieveJDOObjects(
			Set<PriceConfigID> dynamicTradePriceConfigIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicTradePriceConfigs", 1);
		try {
			DynamicTradeManager vm = dynamicTradeManager;
			if (vm == null)
				vm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getDynamicTradePriceConfigs(dynamicTradePriceConfigIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManager dynamicTradeManager;

	public List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<PriceConfigID> dynamicTradePriceConfigIDs = dynamicTradeManager.getDynamicTradePriceConfigIDs();
				return getJDOObjects(null, dynamicTradePriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				dynamicTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<DynamicTradePriceConfig> getDynamicTradePriceConfigs(Collection<PriceConfigID> dynamicTradePriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicTradePriceConfigIDs == null)
			throw new IllegalArgumentException("dynamicTradePriceConfigIDs must not be null!");

		return getJDOObjects(null, dynamicTradePriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public DynamicTradePriceConfig getDynamicTradePriceConfig(PriceConfigID dynamicTradePriceConfigID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicTradePriceConfigID == null)
			throw new IllegalArgumentException("dynamicTradePriceConfigID must not be null!");

		return getJDOObject(null, dynamicTradePriceConfigID, fetchGroups, maxFetchDepth, monitor);
	}
}
