package org.nightlabs.jfire.dunning.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dunning.DunningConfigCustomer;
import org.nightlabs.jfire.dunning.DunningManagerRemote;
import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link DunningConfigCustomer}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DunningConfigCustomerDAO 
extends BaseJDOObjectDAO<DunningConfigCustomerID, DunningConfigCustomer>
{
	private static DunningConfigCustomerDAO sharedInstance = null;

	public static DunningConfigCustomerDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DunningConfigCustomerDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DunningConfigCustomerDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<DunningConfigCustomer> retrieveJDOObjects(Set<DunningConfigCustomerID> dunningConfigCustomerIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading DunningConfigCustomers", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			return im.getDunningConfigCustomers(dunningConfigCustomerIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized DunningConfigCustomer getDunningConfigCustomer(DunningConfigCustomerID dunningConfigCustomerID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningConfigCustomer " + dunningConfigCustomerID.dunningConfigCustomerID, 1);
		DunningConfigCustomer dunningConfigCustomer = getJDOObject(null, dunningConfigCustomerID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return dunningConfigCustomer;
	}

	public synchronized List<DunningConfigCustomer> getDunningConfigCustomers(Set<DunningConfigCustomerID> dunningConfigCustomerIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, dunningConfigCustomerIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized Collection<DunningConfigCustomer> getDunningConfigCustomers(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading dunningConfigCustomers", 1);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			Set<DunningConfigCustomerID> is = im.getDunningConfigCustomerIDs();
			monitor.done();
			return getJDOObjects(null, is, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized DunningConfigCustomer storeDunningConfigCustomer(DunningConfigCustomer dunningConfigCustomer, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(dunningConfigCustomer == null)
			throw new NullPointerException("DunningConfigCustomer to save must not be null");
		monitor.beginTask("Storing dunningConfigCustomer: "+ dunningConfigCustomer.getDunningConfigCustomerID(), 3);
		try {
			DunningManagerRemote im = JFireEjb3Factory.getRemoteBean(
					DunningManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties()
			);
			monitor.worked(1);

			DunningConfigCustomer result = im.storeDunningConfigCustomer(dunningConfigCustomer, get, fetchGroups, maxFetchDepth);
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