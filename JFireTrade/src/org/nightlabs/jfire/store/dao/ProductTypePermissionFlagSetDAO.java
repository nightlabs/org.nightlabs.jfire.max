package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class ProductTypePermissionFlagSetDAO
extends BaseJDOObjectDAO<ProductTypePermissionFlagSetID, ProductTypePermissionFlagSet>
{
	private static volatile ProductTypePermissionFlagSetDAO _sharedInstance = null;

	public static ProductTypePermissionFlagSetDAO sharedInstance()
	{
		if (_sharedInstance == null) {
			synchronized (ProductTypePermissionFlagSetDAO.class) {
				if (_sharedInstance == null)
					_sharedInstance = new ProductTypePermissionFlagSetDAO();
			}
		}
		return _sharedInstance;
	}

	private StoreManagerRemote storeManager;

	@Override
	protected Collection<ProductTypePermissionFlagSet> retrieveJDOObjects(
			Set<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		StoreManagerRemote sm = storeManager;
		if (sm == null)
			sm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);

		return sm.getProductTypePermissionFlagSets(productTypePermissionFlagSetIDs);
	}

	private static final String[] FETCH_GROUPS_INTERNAL = { FetchPlan.DEFAULT };

	public synchronized Collection<ProductTypePermissionFlagSet> getMyProductTypePermissionFlagSets(
			Collection<? extends ProductTypeID> productTypeIDs,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Fetching ProductTypePermissionFlagSets", 100);
		try {
			storeManager = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
			try {
				monitor.worked(10);
				Collection<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs = storeManager.getMyProductTypePermissionFlagSetIDs(productTypeIDs);
				monitor.worked(40);
				return getJDOObjects(
						null,
						productTypePermissionFlagSetIDs,
						FETCH_GROUPS_INTERNAL,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 50)
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				storeManager = null;
			}
		} finally {
			monitor.done();
		}
	}
}
