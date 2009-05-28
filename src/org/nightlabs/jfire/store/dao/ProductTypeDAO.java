/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, ProductType>
{
	protected ProductTypeDAO() {
	}

	/**
	 * The static singleton instance member.
	 */
	private static ProductTypeDAO sharedInstance;

	private StoreManagerRemote storeManager;

	/**
	 * Returns the static singleton (shared instance) of {@link ProductTypeDAO}.
	 * @return the static singleton (shared instance)
	 */
	public static ProductTypeDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ProductTypeDAO();
		return sharedInstance;
	}

	/**
	 * This method returns a new instance of <tt>Set</tt> with those
	 * fetch groups that should always be used as a minimum.
	 * <p>
	 * Overwrite this method if you want to return more fetchgroups.
	 *
	 * @return Returns a <tt>Set</tt> with <tt>FetchPlan.DEFAULT</tt> and <tt>FetchPlan.VALUES</tt>.
	 *
	 * @see #getProductType(ProductTypeID, String[])
	 */
	protected Set<String> getMinimumFetchGroups()
	{
		Set<String> fgSet = new HashSet<String>();
		fgSet.add(FetchPlan.DEFAULT);
		return fgSet;
	}

	/**
	 * @param productTypeID
	 * @param The fetchGroups returned by {@link #getMinimumFetchGroups()} are always
	 *		included. You only need to specify additional fetchgroups or leave
	 *		this <tt>null</tt>.
	 * @return An instance of <tt>ProductType</tt> either from the <tt>Cache</tt> or
	 *		from the server.
	 */
	public ProductType getProductType(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor progressMonitor)
	{
		Set<String> fgSet = getMinimumFetchGroups();

		if (fetchGroups == null)
			fetchGroups = fgSet.toArray(new String[fgSet.size()]);
		else {
			fgSet.addAll(Arrays.asList(fetchGroups));
			fetchGroups = fgSet.toArray(new String[fgSet.size()]);
		}

		return getJDOObject(null, productTypeID, fetchGroups, maxFetchDepth, progressMonitor);
	}

	@Override
	protected Collection<ProductType> retrieveJDOObjects(Set<ProductTypeID> objectIDs, String[] fetchGroups,
	int maxFetchDepth, ProgressMonitor progressMonitor)
	throws Exception
	{
		progressMonitor.beginTask("Loading ProductTypes", 200);
		StoreManagerRemote sm = storeManager;
		if (sm == null)
			sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());

		progressMonitor.worked(100);
		Collection<ProductType> productTypes = sm.getProductTypes(objectIDs, fetchGroups, maxFetchDepth);
		progressMonitor.worked(100);
		return productTypes;
	}


	//Optimized method that returns the ProductTypes using the QueryCollection
	//and retrieving the Products Id
	public synchronized List<ProductType> queryProductTypes(QueryCollection<?> queryCollection, String[] fetchGroups, // TODO correct type for QueryCollection!
	int maxFetchDepth, ProgressMonitor progressMonitor) throws Exception
	{
		storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
		try {
			@SuppressWarnings("unchecked")
			QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries = (QueryCollection<? extends AbstractProductTypeQuery>) queryCollection;
			Collection<ProductTypeID> productTypeIDs = storeManager.getProductTypeIDs(productTypeQueries);
			return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, progressMonitor);
		} finally {
			storeManager = null;
			progressMonitor.done();
		}
	}

	/**
	 * Returns a List of {@link ProductType}s for the given {@link ProductTypeID}s.
	 * @param productTypeIDs the {@link Set} of {@link ProductTypeID} to get the {@link ProductType}s for.
	 * @param fetchGroups the fetchGroups which control which fields should be detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached object graph.
	 * @param progressMonitor the {@link ProgressMonitor} which display the progress of loading.
	 * @return the List of {@link ProductType}s for the given {@link ProductTypeID}s.
	 */
	public List<ProductType> getProductTypes(Collection<ProductTypeID> productTypeIDs, String[] fetchGroups,
	int maxFetchDepth, ProgressMonitor progressMonitor)
	{
		return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, progressMonitor);
	}

	public synchronized List<ProductType> getRootProductTypes(
			Class<? extends ProductType> productTypeClass, boolean subclasses,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProductTypeID> productTypeIDs = storeManager.getRootProductTypeIDs(productTypeClass, subclasses);
				return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				storeManager = null;
				monitor.done();
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * Returns a List of child {@link ProductType}s for the given {@link ProductTypeID}.
	 * @param parentProductTypeID the {@link ProductTypeID} of the parent {@link ProductType} to get the children for.
	 * @param fetchGroups the fetchGroups which control which fields should be detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached object graph.
	 * @param monitor the {@link ProgressMonitor} which display the progress of loading.
	 * @return a List of child {@link ProductType}s for the given {@link ProductTypeID}.
	 */
	public synchronized List<ProductType> getChildProductTypes(ProductTypeID parentProductTypeID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProductTypeID> productTypeIDs = storeManager.getChildProductTypeIDs(parentProductTypeID);
				return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				storeManager = null;
				monitor.done();
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	
	/**
	 * Returns a List of child {@link ProductTypeIDs}s for the given Parent {@link ProductTypeID}.
	 * @param parentProductTypeID the {@link ProductTypeID} of the parent {@link ProductType} to get the children for.
	 * @param fetchGroups the fetchGroups which control which fields should be detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached object graph.
	 * @param monitor the {@link ProgressMonitor} which display the progress of loading.
	 * @return a List of child IDs {@link ProductTypeID}s for the given Parent {@link ProductTypeID}.
	 */
	public synchronized Collection<ProductTypeID> getChildProductTypesIDs(ProductTypeID parentProductTypeID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProductTypeID> productTypeIDs = storeManager.getChildProductTypeIDs(parentProductTypeID);
				return productTypeIDs;
			} finally {
				storeManager = null;
				monitor.done();
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	/**
	 * Returns the total number of Child count.
	 * @param parentProductTypeID the {@link ProductTypeID} of the parent {@link ProductType} to get the children for.
	 * @param monitor the {@link ProgressMonitor} which display the progress of loading.
	 * @return the total number of Child count{@link ProductTypeID}s for the given Parent {@link ProductTypeID}.
	 */	   
	public Map<ProductTypeID, Long> getChildProductTypeCounts(Collection<ProductTypeID> parentProductTypeIDs, ProgressMonitor monitor)	
	{	
		try {
			storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {			
				return storeManager.getChildProductTypeCounts(parentProductTypeIDs);			
			} finally {
				storeManager = null;
				monitor.done();
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		
	}
}
