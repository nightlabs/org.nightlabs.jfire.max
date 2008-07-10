/**
 * 
 */
package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.search.AbstractProductQuery;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A Generic DAO (Data Access Object) for retrieving {@link Product}s.
 *  
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductDAO 
extends BaseJDOObjectDAO<ProductID, Product> 
{
	private static ProductDAO sharedInstance = null;
	/**
	 * Returns the sharedInstance (singleton) of this DAO
	 * @return the sharedInstance (singleton) of this DAO
	 */
	public static ProductDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ProductDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProductDAO();
			}
		}
		return sharedInstance;
	}

	/**
	 * Returns a Collection all {@link Product}s with the given {@link ProductID}s and the data determined by the fetchGroups and fetchDepth.
	 * 
	 * @param productIDs a Set of {@link ProductID} to get the corresponding {@link Product}s for
	 * @param fetchGroups the JDO FetchGroups
	 * @param maxFetchDepth the maximum fetchDepth
	 * @param monitor the {@link ProgressMonitor} to display the progress
	 * @return a Collection all {@link Product}s with the given {@link ProductID}s and the data determined by the fetchGroups and fetchDepth 
	 */
	public Collection<Product> getProducts(Set<ProductID> productIDs, String[] fetchGroups, 
			int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, productIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns a Collection all {@link Product}s which match the criteria determined
	 * in the given {@link QueryCollection}.
	 * 
	 * @param queryCollection a QueryCollection containing the queries for searching the products 
	 * @param fetchGroups the JDO FetchGroups
	 * @param maxFetchDepth the maximum fetchDepth
	 * @param monitor the {@link ProgressMonitor} to display the progress
	 * @return a Collection all {@link Product}s which match the criteria determined
	 * in the given {@link QueryCollection} with the data determined by the fetchGroups and fetchDepth
	 */
	public Collection<Product> getProducts(QueryCollection<? extends AbstractProductQuery> queryCollection, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor progressMonitor)throws Exception
	{
		StoreManager storeManager = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		try {	
			Set<ProductID> productIDs = storeManager.getProductIDs(queryCollection);
			return getJDOObjects(null, productIDs, fetchGroups, maxFetchDepth, progressMonitor);
		} finally {
			storeManager = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<Product> retrieveJDOObjects(Set<ProductID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception 
	{
		monitor.beginTask("Loading Products", 200);
		StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		monitor.worked(100);

		Collection<Product> products = sm.getProducts(objectIDs, fetchGroups, maxFetchDepth);
		monitor.worked(100);
		return products;
	}

}
