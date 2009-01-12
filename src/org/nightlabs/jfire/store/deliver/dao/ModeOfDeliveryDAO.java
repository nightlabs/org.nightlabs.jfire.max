package org.nightlabs.jfire.store.deliver.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
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

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ModeOfDelivery> retrieveJDOObjects(
			Set<ModeOfDeliveryID> ModeOfDeliveryIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManager am = accountingManager;
		if (am == null)
			am = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());

		return am.getModeOfDeliverys(ModeOfDeliveryIDs, fetchGroups, maxFetchDepth);
	}

	private StoreManager accountingManager;

	@SuppressWarnings("unchecked")
	public synchronized List<ModeOfDelivery> getModeOfDeliverys(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			accountingManager = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<ModeOfDeliveryID> ModeOfDeliveryIDs = accountingManager.getAllModeOfDeliveryIDs();
				return getJDOObjects(null, ModeOfDeliveryIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				accountingManager = null;
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
