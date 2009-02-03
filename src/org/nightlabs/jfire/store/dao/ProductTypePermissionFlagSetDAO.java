package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class ProductTypePermissionFlagSetDAO
extends BaseJDOObjectDAO<ProductTypePermissionFlagSetID, ProductTypePermissionFlagSet>
{
	private StoreManager storeManager;

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ProductTypePermissionFlagSet> retrieveJDOObjects(
			Set<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		StoreManager sm = storeManager;
		if (sm == null)
			sm = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());

		return sm.getProductTypePermissionFlagSets(productTypePermissionFlagSetIDs);
	}

	private static final String[] FETCH_GROUPS_INTERNAL = { FetchPlan.DEFAULT };

	@SuppressWarnings("unchecked")
	public synchronized Collection<ProductTypePermissionFlagSet> getMyProductTypePermissionFlagSets(
			Collection<? extends ProductTypeID> productTypeIDs,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Fetching ProductTypePermissionFlagSets", 100);
		try {
			storeManager = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
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
