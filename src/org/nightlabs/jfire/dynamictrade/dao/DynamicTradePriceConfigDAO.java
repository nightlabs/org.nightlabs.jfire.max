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

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<DynamicTradePriceConfig> retrieveJDOObjects(
			Set<PriceConfigID> dynamicPriceConfigIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicPriceConfigs", 1);
		try {
			DynamicTradeManager vm = dynamicTradeManager;
			if (vm == null)
				vm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getDynamicPriceConfigs(dynamicPriceConfigIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManager dynamicTradeManager;

	@SuppressWarnings("unchecked")
	public List<DynamicTradePriceConfig> getDynamicPriceConfigs(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<PriceConfigID> dynamicPriceConfigIDs = dynamicTradeManager.getDynamicPriceConfigIDs();
				return getJDOObjects(null, dynamicPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				dynamicTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<DynamicTradePriceConfig> getDynamicPriceConfigs(Collection<PriceConfigID> dynamicPriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicPriceConfigIDs == null)
			throw new IllegalArgumentException("dynamicPriceConfigIDs must not be null!");

		return getJDOObjects(null, dynamicPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public DynamicTradePriceConfig getDynamicPriceConfig(PriceConfigID dynamicPriceConfigID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicPriceConfigID == null)
			throw new IllegalArgumentException("dynamicPriceConfigID must not be null!");

		return getJDOObject(null, dynamicPriceConfigID, fetchGroups, maxFetchDepth, monitor);
	}
}
