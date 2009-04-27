package org.nightlabs.jfire.simpletrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

public class SimpleProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, SimpleProductType>
implements IJDOObjectDAO<SimpleProductType>
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

	@Override
	protected Collection<SimpleProductType> retrieveJDOObjects(
			Set<ProductTypeID> simpleProductTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading SimpleProductTypes", 1);
		try {
//			SimpleTradeManager vm = simpleTradeManager;
//			if (vm == null)
//				vm = JFireEjbFactory.getBean(SimpleTradeManager.class, SecurityReflector.getInitialContextProperties());
//
//			return vm.getSimpleProductTypes(simpleProductTypeIDs, fetchGroups, maxFetchDepth);
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return CollectionUtil.castCollection(sm.getProductTypes(simpleProductTypeIDs, fetchGroups, maxFetchDepth));
		} finally {
			monitor.worked(1);
		}
	}

	private SimpleTradeManagerRemote simpleTradeManager;

	public synchronized List<SimpleProductType> getChildSimpleProductTypes(ProductTypeID parentSimpleProductTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			simpleTradeManager = JFireEjb3Factory.getRemoteBean(SimpleTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
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

	/**
	 * Store a {@link SimpleProductType} and its properties on the server.
	 * @param productType the ProductType to save
	 * @param monitor The progress monitor to display the progress
	 */
	public synchronized void storeSimpleProductType(SimpleProductType productType, ProgressMonitor monitor)
	{
		storeJDOObject(productType, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.jdo.IJDOObjectDAO#storeJDOObject(java.lang.Object, boolean, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public SimpleProductType storeJDOObject(SimpleProductType jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		if(jdoObject == null)
			throw new NullPointerException("SimpleProductType to save must not be null");
//		if (!(jdoObject instanceof SimpleProductType))
//			throw new IllegalArgumentException("ProductType to save must be of type " + SimpleProductType.class.getSimpleName() + ", passed was " + jdoObject.getClass().getSimpleName());
		SimpleProductType productType = Util.cloneSerializable(jdoObject);
		SimpleProductType result = null;
		monitor.beginTask("Saving SimpleProductType PropertySet", 3);
		Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
		try {
			PropertySet propertySet = null;
			try {
				productType.getPropertySet();
			} catch (JDODetachedFieldAccessException e) {
				// propertySet not detached
				propertySet = null;
			}
			if (propertySet != null) {
				propertySet.deflate();
			}
			SimpleTradeManagerRemote simpleTradeManager = JFireEjb3Factory.getRemoteBean(SimpleTradeManagerRemote.class, initialContextProperties);
			result = simpleTradeManager.storeProductType(productType, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
		} catch(Exception e) {
			monitor.done();
			throw new RuntimeException("PropertySet Save failed", e);
		}
		return result;
	}
}
