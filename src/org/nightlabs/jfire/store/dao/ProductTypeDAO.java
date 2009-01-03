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
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.id.ProductTypeID;
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

	private static ProductTypeDAO sharedInstance;

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

	// TODO: Implement Authority checking (needs to be in the EJB!)
	@Override
	protected Collection<ProductType> retrieveJDOObjects(Set<ProductTypeID> objectIDs, String[] fetchGroups,
	int maxFetchDepth, ProgressMonitor progressMonitor)
	throws Exception
	{
		progressMonitor.beginTask("Loading ProductTypes", 200);
		StoreManager sm = storeManager;
		if (sm == null)
			sm = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());

		progressMonitor.worked(100);
		Collection<ProductType> productTypes = sm.getProductTypes(objectIDs, fetchGroups, maxFetchDepth);
		progressMonitor.worked(100);
		return productTypes;
	}

	private StoreManager storeManager;


	//Optimized method that returns the ProductTypes using the QueryCollection
	//and retrieving the Products Id

	public synchronized List<ProductType> getProductTypes(QueryCollection<?> queryCollection, String[] fetchGroups,
	int maxFetchDepth, ProgressMonitor progressMonitor)throws Exception
	{
		storeManager = JFireEjbFactory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
		try {	
			Set<ProductTypeID> productTypeIDs = storeManager.getProductTypeIDs(queryCollection);
			return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, progressMonitor);
		} finally {
			storeManager = null;
		}
	}

	// TODO: Implement authority checking - should be done in the server!
	public List<ProductType> getProductTypes(Set<ProductTypeID> productTypeIDs, String[] fetchGroups,
	int maxFetchDepth, ProgressMonitor progressMonitor)
	{
		return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, progressMonitor);
	}

}
