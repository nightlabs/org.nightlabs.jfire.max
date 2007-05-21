package org.nightlabs.jfire.swifttrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.swifttrade.SwiftTradeManager;
import org.nightlabs.jfire.swifttrade.SwiftTradeManagerUtil;
import org.nightlabs.jfire.swifttrade.store.SwiftProductType;
import org.nightlabs.progress.ProgressMonitor;

public class SwiftProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, SwiftProductType>
{
	private static SwiftProductTypeDAO sharedInstance = null;

	public static SwiftProductTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (SwiftProductTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new SwiftProductTypeDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<SwiftProductType> retrieveJDOObjects(
			Set<ProductTypeID> swiftProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading SwiftProductTypes", 1);
		try {
			SwiftTradeManager vm = swiftTradeManager;
			if (vm == null)
				vm = SwiftTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getSwiftProductTypes(swiftProductTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private SwiftTradeManager swiftTradeManager;

	@SuppressWarnings("unchecked")
	public synchronized List<SwiftProductType> getChildSwiftProductTypes(ProductTypeID parentSwiftProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			swiftTradeManager = SwiftTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> swiftProductTypeIDs = swiftTradeManager.getChildSwiftProductTypeIDs(parentSwiftProductTypeID);
				return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@SuppressWarnings("unchecked")
	public List<SwiftProductType> getSwiftProductTypes(Byte inheritanceNature, Boolean saleable, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			swiftTradeManager = SwiftTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> swiftProductTypeIDs = swiftTradeManager.getSwiftProductTypeIDs(inheritanceNature, saleable);
				return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				swiftTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<SwiftProductType> getSwiftProductTypes(Collection<ProductTypeID> swiftProductTypeIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftProductTypeIDs == null)
			throw new IllegalArgumentException("swiftProductTypeIDs must not be null!");

		return getJDOObjects(null, swiftProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public SwiftProductType getSwiftProductType(ProductTypeID swiftProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (swiftProductTypeID == null)
			throw new IllegalArgumentException("swiftProductTypeID must not be null!");

		return getJDOObject(null, swiftProductTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
