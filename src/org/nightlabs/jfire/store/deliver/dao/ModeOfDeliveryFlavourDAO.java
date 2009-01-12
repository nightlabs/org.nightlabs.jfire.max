package org.nightlabs.jfire.store.deliver.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
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

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ModeOfDeliveryFlavour> retrieveJDOObjects(
			Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManager am = storeManager;
		if (am == null)
			am = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());

		return am.getModeOfDeliveryFlavours(modeOfDeliveryFlavourIDs, fetchGroups, maxFetchDepth);
	}

	private StoreManager storeManager;

	@SuppressWarnings("unchecked")
	public synchronized List<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
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
