package org.nightlabs.jfire.dunning.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dunning.DunningFeeAdder;
import org.nightlabs.jfire.dunning.DunningManagerRemote;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link DunningFeeAdder}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DunningFeeAdderDAO 
extends BaseJDOObjectDAO<DunningFeeAdderID, DunningFeeAdder>
{
	private static DunningFeeAdderDAO sharedInstance = null;

	public static DunningFeeAdderDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DunningFeeAdderDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DunningFeeAdderDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<DunningFeeAdder> retrieveJDOObjects(Set<DunningFeeAdderID> dunningFeeAdderIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading DunningFeeAdders", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(DunningManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getDunningFeeAdders(dunningFeeAdderIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized DunningFeeAdder getDunningFeeAdder(DunningFeeAdderID dunningFeeAdderID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningFeeAdder " + dunningFeeAdderID.dunningFeeAdderID, 1);
		DunningFeeAdder dunningFeeAdder = getJDOObject(null, dunningFeeAdderID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return dunningFeeAdder;
	}
}