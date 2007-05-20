package org.nightlabs.jfire.simpletrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class SimpleProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, SimpleProductType>
{
	private static SimpleProductTypeDAO sharedInstance = null;

	public static SimpleProductTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (SimpleProductTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new SimpleProductTypeDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<SimpleProductType> retrieveJDOObjects(
			Set<ProductTypeID> simpleProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading SimpleProductTypes", 1);
		try {
			SimpleTradeManager vm = simpleTradeManager;
			if (vm == null)
				vm = SimpleTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getSimpleProductTypes(simpleProductTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private SimpleTradeManager simpleTradeManager;

	@SuppressWarnings("unchecked")
	public synchronized List<SimpleProductType> getChildSimpleProductTypes(ProductTypeID parentSimpleProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			simpleTradeManager = SimpleTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> simpleProductTypeIDs = simpleTradeManager.getChildSimpleProductTypeIDs(parentSimpleProductTypeID);
				return getJDOObjects(null, simpleProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				simpleTradeManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<SimpleProductType> getSimpleProductTypes(Collection<ProductTypeID> simpleProductTypeIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (simpleProductTypeIDs == null)
			throw new IllegalArgumentException("simpleProductTypeIDs must not be null!");

		return getJDOObjects(null, simpleProductTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public SimpleProductType getSimpleProductType(ProductTypeID simpleProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (simpleProductTypeID == null)
			throw new IllegalArgumentException("simpleProductTypeID must not be null!");

		return getJDOObject(null, simpleProductTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
