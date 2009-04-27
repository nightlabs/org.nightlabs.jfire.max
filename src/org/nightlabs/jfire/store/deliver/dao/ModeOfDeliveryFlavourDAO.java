package org.nightlabs.jfire.store.deliver.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.progress.ProgressMonitor;

/**
 *
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class ModeOfDeliveryFlavourDAO
		extends BaseJDOObjectDAO<ModeOfDeliveryFlavourID, ModeOfDeliveryFlavour>
{
	private static ModeOfDeliveryFlavourDAO sharedInstance = null;

	public synchronized static ModeOfDeliveryFlavourDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ModeOfDeliveryFlavourDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<ModeOfDeliveryFlavour> retrieveJDOObjects(
			Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManagerRemote sm = storeManager;
		if (sm == null)
			sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return sm.getModeOfDeliveryFlavours(modeOfDeliveryFlavourIDs, fetchGroups, maxFetchDepth);
	}

	private StoreManagerRemote storeManager;

	public synchronized List<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<ModeOfDeliveryFlavourID> tariffIDs = storeManager.getAllModeOfDeliveryFlavourIDs();
				return getJDOObjects(null, tariffIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				storeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours(Set<ModeOfDeliveryFlavourID> ModeOfDeliveryFlavourIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, ModeOfDeliveryFlavourIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
