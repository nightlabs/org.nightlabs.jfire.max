package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManager;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class DynamicProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, DynamicProductType>
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
			Set<ProductTypeID> swiftProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading DynamicProductTypes", 1);
		try {
			DynamicTradeManager vm = swiftTradeManager;
			if (vm == null)
				vm = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getDynamicProductTypes(swiftProductTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private DynamicTradeManager swiftTradeManager;

	@SuppressWarnings("unchecked")
	public synchronized List<DynamicProductType> getChildDynamicProductTypes(ProductTypeID parentDynamicProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			swiftTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> swiftProductTypeIDs = swiftTradeManager.getChildDynamicProductTypeIDs(parentDynamicProductTypeID);
				return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
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
			swiftTradeManager = DynamicTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> swiftProductTypeIDs = swiftTradeManager.getDynamicProductTypeIDs(inheritanceNature, saleable);
				return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<DynamicProductType> getDynamicProductTypes(Collection<ProductTypeID> swiftProductTypeIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftProductTypeIDs == null)
			throw new IllegalArgumentException("swiftProductTypeIDs must not be null!");

		return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public DynamicProductType getDynamicProductType(ProductTypeID swiftProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftProductTypeID == null)
			throw new IllegalArgumentException("swiftProductTypeID must not be null!");

		return getJDOObject(null, swiftProductTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
