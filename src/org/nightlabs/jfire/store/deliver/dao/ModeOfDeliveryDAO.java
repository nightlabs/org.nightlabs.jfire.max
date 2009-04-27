package org.nightlabs.jfire.store.deliver.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.progress.ProgressMonitor;

/**
 *
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class ModeOfDeliveryDAO
		extends BaseJDOObjectDAO<ModeOfDeliveryID, ModeOfDelivery>
{
	private static ModeOfDeliveryDAO sharedInstance = null;

	public synchronized static ModeOfDeliveryDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ModeOfDeliveryDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<ModeOfDelivery> retrieveJDOObjects(
			Set<ModeOfDeliveryID> ModeOfDeliveryIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManagerRemote sm = storeManager;
		if (sm == null)
			sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return sm.getModeOfDeliverys(ModeOfDeliveryIDs, fetchGroups, maxFetchDepth);
	}

	private StoreManagerRemote storeManager;

	public synchronized List<ModeOfDelivery> getModeOfDeliverys(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<ModeOfDeliveryID> ModeOfDeliveryIDs = storeManager.getAllModeOfDeliveryIDs();
				return getJDOObjects(null, ModeOfDeliveryIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				storeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<ModeOfDelivery> getModeOfDeliverys(Set<ModeOfDeliveryID> ModeOfDeliveryIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, ModeOfDeliveryIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
