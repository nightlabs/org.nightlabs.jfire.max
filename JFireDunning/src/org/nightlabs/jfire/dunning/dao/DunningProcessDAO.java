package org.nightlabs.jfire.dunning.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dunning.DunningManagerRemote;
import org.nightlabs.jfire.dunning.DunningProcess;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link DunningProcess}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DunningProcessDAO
	extends BaseJDOObjectDAO<DunningProcessID, DunningProcess>
{

	private static DunningProcessDAO sharedInstance = null;

	public static DunningProcessDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DunningProcessDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DunningProcessDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<DunningProcess> retrieveJDOObjects(Set<DunningProcessID> dunningProcessIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading DunningProcesss", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			return im.getDunningProcesses(dunningProcessIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized DunningProcess getDunningProcess(DunningProcessID dunningProcessID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningProcess ", 1);
		DunningProcess dunningProcess = getJDOObject(null, dunningProcessID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return dunningProcess;
	}

	public synchronized List<DunningProcess> getDunningProcesses(Set<DunningProcessID> dunningProcessIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, dunningProcessIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized Collection<DunningProcess> getDunningProcesses(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningProcessses", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			Set<DunningProcessID> is = im.getDunningProcessIDs();
			monitor.done();
			return getJDOObjects(null, is, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
