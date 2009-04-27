package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
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
	protected Collection<Unit> retrieveJDOObjects(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		StoreManagerRemote stm = storeManager;
		if (stm == null)
			stm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return stm.getUnits(unitIDs, fetchGroups, maxFetchDepth);
	}

	public List<Unit> getUnits(Set<UnitID> unitIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (unitIDs == null)
			return getUnits(fetchGroups, maxFetchDepth, monitor);

		return getJDOObjects(null, unitIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private StoreManagerRemote storeManager = null;

	public List<Unit> getUnits(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
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
