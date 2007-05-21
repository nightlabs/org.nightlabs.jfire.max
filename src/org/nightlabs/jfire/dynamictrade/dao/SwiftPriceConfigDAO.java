package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.SwiftTradeManager;
import org.nightlabs.jfire.dynamictrade.SwiftTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.SwiftPriceConfig;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class SwiftPriceConfigDAO
extends BaseJDOObjectDAO<PriceConfigID, SwiftPriceConfig>
{
	private static SwiftPriceConfigDAO sharedInstance = null;

	public static SwiftPriceConfigDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (SwiftPriceConfigDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new SwiftPriceConfigDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<SwiftPriceConfig> retrieveJDOObjects(
			Set<PriceConfigID> swiftPriceConfigIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading SwiftPriceConfigs", 1);
		try {
			SwiftTradeManager vm = swiftTradeManager;
			if (vm == null)
				vm = SwiftTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getSwiftPriceConfigs(swiftPriceConfigIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private SwiftTradeManager swiftTradeManager;

	@SuppressWarnings("unchecked")
	public List<SwiftPriceConfig> getSwiftPriceConfigs(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			swiftTradeManager = SwiftTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<PriceConfigID> swiftPriceConfigIDs = swiftTradeManager.getSwiftPriceConfigIDs();
				return getJDOObjects(null, swiftPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<SwiftPriceConfig> getSwiftPriceConfigs(Collection<PriceConfigID> swiftPriceConfigIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftPriceConfigIDs == null)
			throw new IllegalArgumentException("swiftPriceConfigIDs must not be null!");

		return getJDOObjects(null, swiftPriceConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public SwiftPriceConfig getSwiftPriceConfig(PriceConfigID swiftPriceConfigID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftPriceConfigID == null)
			throw new IllegalArgumentException("swiftPriceConfigID must not be null!");

		return getJDOObject(null, swiftPriceConfigID, fetchGroups, maxFetchDepth, monitor);
	}
}
