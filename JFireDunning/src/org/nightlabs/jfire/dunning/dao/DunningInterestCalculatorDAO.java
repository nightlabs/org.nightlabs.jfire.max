package org.nightlabs.jfire.dunning.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dunning.DunningInterestCalculator;
import org.nightlabs.jfire.dunning.DunningManagerRemote;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link DunningInterestCalculator}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DunningInterestCalculatorDAO 
extends BaseJDOObjectDAO<DunningInterestCalculatorID, DunningInterestCalculator>
{
	private static DunningInterestCalculatorDAO sharedInstance = null;

	public static DunningInterestCalculatorDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DunningInterestCalculatorDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DunningInterestCalculatorDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<DunningInterestCalculator> retrieveJDOObjects(Set<DunningInterestCalculatorID> dunningInterestCalculatorIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading DunningInterestCalculators", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			return im.getDunningInterestCalculators(dunningInterestCalculatorIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized DunningInterestCalculator getDunningInterestCalculator(DunningInterestCalculatorID dunningInterestCalculatorID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningInterestCalculator " + dunningInterestCalculatorID.dunningInterestCalculatorID, 1);
		DunningInterestCalculator dunningInterestCalculator = getJDOObject(null, dunningInterestCalculatorID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return dunningInterestCalculator;
	}
}