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
			Set<PriceConfigID> swiftPriceConfigIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicPriceConfigs", 1);
		try {
			DynamicTradeManager vm = swiftTradeManager;
			if (vm == null)
				vm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getDynamicPriceConfigs(swiftPriceConfigIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManager swiftTradeManager;

	@SuppressWarnings("unchecked")
	public List<DynamicTradePriceConfig> getDynamicPriceConfigs(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			swiftTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<PriceConfigID> swiftPriceConfigIDs = swiftTradeManager.getDynamicPriceConfigIDs();
				return getJDOObjects(null, swiftPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<DynamicTradePriceConfig> getDynamicPriceConfigs(Collection<PriceConfigID> swiftPriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftPriceConfigIDs == null)
			throw new IllegalArgumentException("swiftPriceConfigIDs must not be null!");

		return getJDOObjects(null, swiftPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public DynamicTradePriceConfig getDynamicPriceConfig(PriceConfigID swiftPriceConfigID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftPriceConfigID == null)
			throw new IllegalArgumentException("swiftPriceConfigID must not be null!");

		return getJDOObject(null, swiftPriceConfigID, fetchGroups, maxFetchDepth, monitor);
	}
}
