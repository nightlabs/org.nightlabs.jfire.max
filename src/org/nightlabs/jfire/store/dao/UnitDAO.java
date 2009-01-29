package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.id.UnitID;
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
	protected Collection<Unit> retrieveJDOObjects(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		StoreManager stm = storeManager;
		if (stm == null)
			stm = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());

		return stm.getUnits(unitIDs, fetchGroups, maxFetchDepth);
	}

	public List<Unit> getUnits(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (unitIDs == null)
			return getUnits(fetchGroups, maxFetchDepth, monitor);

		return getJDOObjects(null, unitIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private StoreManager storeManager = null;

	@SuppressWarnings("unchecked")
	public List<Unit> getUnits(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<UnitID> unitIDs = storeManager.getUnitIDs();
				return getJDOObjects(null, unitIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				storeManager = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
