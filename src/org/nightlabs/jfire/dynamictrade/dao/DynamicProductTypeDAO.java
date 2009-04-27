package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerRemote;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

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

	@Override
	protected Collection<DynamicProductType> retrieveJDOObjects(
			Set<ProductTypeID> dynamicProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicProductTypes", 1);
		try {
//			DynamicTradeManager vm = dynamicTradeManager;
//			if (vm == null)
//				vm = JFireEjbFactory.getBean(DynamicTradeManager.class, SecurityReflector.getInitialContextProperties());
//
//			return vm.getDynamicProductTypes(dynamicProductTypeIDs, fetchGroups, maxFetchDepth);
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return CollectionUtil.castCollection(sm.getProductTypes(dynamicProductTypeIDs, fetchGroups, maxFetchDepth));
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManagerRemote dynamicTradeManager;

	public synchronized List<DynamicProductType> getChildDynamicProductTypes(ProductTypeID parentDynamicProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			dynamicTradeManager = JFireEjb3Factory.getRemoteBean(DynamicTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
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

//	public List<DynamicProductType> getDynamicProductTypes(Byte inheritanceNature, Boolean saleable, String[] fetchGroups, int maxFetchDepth,
//			ProgressMonitor monitor)
//	{
//		try {
//			dynamicTradeManager = JFireEjbFactory.getBean(DynamicTradeManager.class, SecurityReflector.getInitialContextProperties());
//			try {
//				Collection<ProductTypeID> dynamicProductTypeIDs = dynamicTradeManager.getDynamicProductTypeIDs(inheritanceNature, saleable);
//				return getJDOObjects(null, dynamicProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
//			} finally {
//				dynamicTradeManager = null;
//			}
//		} catch (Exception x) {
//			throw new RuntimeException(x);
//		}
//	}

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
	@Override
	public DynamicProductType storeJDOObject(DynamicProductType jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			DynamicTradeManagerRemote dtm = JFireEjb3Factory.getRemoteBean(DynamicTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return dtm.storeDynamicProductType(jdoObject, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
