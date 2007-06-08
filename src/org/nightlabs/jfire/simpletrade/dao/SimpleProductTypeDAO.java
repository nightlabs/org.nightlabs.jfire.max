package org.nightlabs.jfire.simpletrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Utils;

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
	
	/**
	 * Store a {@link SimpleProductType} and its properties on the server.
	 * @param productType the ProductType to save
	 * @param monitor The progress monitor to display the progress
	 */
	public synchronized void storeSimpleProductType(SimpleProductType productType, ProgressMonitor monitor)
	{
		if(productType == null)
			throw new NullPointerException("SimpleProductType to save must not be null");

		productType = Utils.cloneSerializable(productType);
		monitor.beginTask("Saving SimpleProductType PropertySet", 3);
		Properties initialContextProperties = SecurityReflector.getInitialContextProperties();		
		try {
			PropertySet propertySet = productType.getPropertySet();
			IStruct struct = propertySet.getStructure();
			if (struct != null)			
				struct.implodeProperty(propertySet);			
//			long activePropertyID = propertySet.getPropertyID();
//			if (activePropertyID == PropertySet.TEMPORARY_PROP_ID) {
//				propertySet.assignID(IDGenerator.nextID(PropertySet.class));
//				PropertyManager pm = PropertyManagerUtil.getHome(initialContextProperties).create();		
//				monitor.worked(1);
//				pm.storeProperty(propertySet, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);				
//				monitor.worked(1);				
//			}
			SimpleTradeManager simpleTradeManager = SimpleTradeManagerUtil.getHome(initialContextProperties).create();
			simpleTradeManager.storeProductType(productType, false, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			monitor.worked(1);
//			struct.explodeProperty(propertySet);    
//			monitor.worked(1);
			monitor.done();
		} catch(Exception e) {
			monitor.done();
			throw new RuntimeException("PropertySet Save failed", e);
		}
	}	
}
