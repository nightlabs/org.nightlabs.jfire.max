package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManager;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.store.Unit;
import org.nightlabs.jfire.dynamictrade.store.id.UnitID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class UnitDAO
extends BaseJDOObjectDAO<UnitID, Unit>
{
	private static UnitDAO sharedInstance = null;

	public static UnitDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (UnitDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new UnitDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<Unit> retrieveJDOObjects(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		DynamicTradeManager stm = dynamicTradeManager;
		if (stm == null)
			stm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

		return stm.getUnits(unitIDs, fetchGroups, maxFetchDepth);
	}

	public List<Unit> getUnits(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (unitIDs == null)
			return getUnits(fetchGroups, maxFetchDepth, monitor);

		return getJDOObjects(null, unitIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private DynamicTradeManager dynamicTradeManager = null;

	@SuppressWarnings("unchecked")
	public List<Unit> getUnits(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Set<UnitID> unitIDs = dynamicTradeManager.getUnitIDs();
				return getJDOObjects(null, unitIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				dynamicTradeManager = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
