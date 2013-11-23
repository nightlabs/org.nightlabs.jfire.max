package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

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
			stm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);

		return stm.getUnits(unitIDs, fetchGroups, maxFetchDepth);
	}

	public synchronized Unit getUnit(UnitID unitID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading unit " + unitID.unitID, 1);
		Unit unit = getJDOObject(null, unitID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return unit;
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
			storeManager = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
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
	
	public synchronized Unit storeUnit(Unit unit, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(unit == null)
			throw new NullPointerException("Unit to save must not be null");
		monitor.beginTask("Storing unit: "+ unit.getUnitID(), 3);
		try {
			StoreManagerRemote sm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
			monitor.worked(1);

			Unit result = sm.storeUnit(unit, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}

}
