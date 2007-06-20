package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManager;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class DynamicProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, DynamicProductType>
implements IJDOObjectDAO<DynamicProductType>
{
	private static DynamicProductTypeDAO sharedInstance = null;

	public static DynamicProductTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DynamicProductTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DynamicProductTypeDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<DynamicProductType> retrieveJDOObjects(
			Set<ProductTypeID> dynamicProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicProductTypes", 1);
		try {
			DynamicTradeManager vm = dynamicTradeManager;
			if (vm == null)
				vm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getDynamicProductTypes(dynamicProductTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManager dynamicTradeManager;

	@SuppressWarnings("unchecked")
	public synchronized List<DynamicProductType> getChildDynamicProductTypes(ProductTypeID parentDynamicProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> dynamicProductTypeIDs = dynamicTradeManager.getChildDynamicProductTypeIDs(parentDynamicProductTypeID);
				return getJDOObjects(null, dynamicProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				dynamicTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@SuppressWarnings("unchecked")
	public List<DynamicProductType> getDynamicProductTypes(Byte inheritanceNature, Boolean saleable, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> dynamicProductTypeIDs = dynamicTradeManager.getDynamicProductTypeIDs(inheritanceNature, saleable);
				return getJDOObjects(null, dynamicProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				dynamicTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<DynamicProductType> getDynamicProductTypes(Collection<ProductTypeID> dynamicProductTypeIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicProductTypeIDs == null)
			throw new IllegalArgumentException("dynamicProductTypeIDs must not be null!");

		return getJDOObjects(null, dynamicProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public DynamicProductType getDynamicProductType(ProductTypeID dynamicProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (dynamicProductTypeID == null)
			throw new IllegalArgumentException("dynamicProductTypeID must not be null!");

		return getJDOObject(null, dynamicProductTypeID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.jdo.IJDOObjectDAO#storeJDOObject(java.lang.Object, boolean, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Implement
	public DynamicProductType storeJDOObject(DynamicProductType jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			DynamicTradeManager dtm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return dtm.storeDynamicProductType(jdoObject, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
