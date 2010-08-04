package org.nightlabs.jfire.dunning.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dunning.DunningConfig;
import org.nightlabs.jfire.dunning.DunningManagerRemote;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link DunningConfig}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DunningConfigDAO extends BaseJDOObjectDAO<DunningConfigID, DunningConfig>{

	private static DunningConfigDAO sharedInstance = null;

	public static DunningConfigDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DunningConfigDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DunningConfigDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<DunningConfig> retrieveJDOObjects(Set<DunningConfigID> dunningConfigIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading DunningConfigs", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(DunningManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getDunningConfigs(dunningConfigIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized DunningConfig getDunningConfig(DunningConfigID dunningConfigID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningConfig " + dunningConfigID.dunningConfigID, 1);
		DunningConfig dunningConfig = getJDOObject(null, dunningConfigID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return dunningConfig;
	}

	public synchronized List<DunningConfig> getDunningConfigs(Set<DunningConfigID> dunningConfigIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, dunningConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized Collection<DunningConfig> getDunningConfigs(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningConfigs", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(DunningManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<DunningConfigID> is = im.getDunningConfigIDs();
			monitor.done();
			return getJDOObjects(null, is, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
