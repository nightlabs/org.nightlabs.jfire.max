/**
 * 
 */
package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductTypeGroup;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductTypeGroupDAO 
extends BaseJDOObjectDAO<ProductTypeGroupID, ProductTypeGroup>
{
	private static ProductTypeGroupDAO sharedInstance = null;

	public static ProductTypeGroupDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (ProductTypeGroupDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProductTypeGroupDAO();
			}
		}
		return sharedInstance;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ProductTypeGroup> retrieveJDOObjects(Set<ProductTypeGroupID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception 
	{
		monitor.beginTask("Loading Repositories", 1);
		try {
			StoreManager am = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return am.getRepositories(objectIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * Returns a {@link List} for all {@link ProductTypeGroup}s with the given {@link ProductTypeGroupID}s.
	 * 
	 * @param productTypeGroupIDs a {@link Set} of the {@link ProductTypeGroupID}s to get the corresponding {@link ProductTypeGroup}s for 
	 * @param fetchGroups the JDO fetch groups
	 * @param maxFetchDepth the maximum fetch depth
	 * @param progressMonitor the {@link ProgressMonitor} for displaying the progress
	 * @return a {@link List} for all {@link ProductTypeGroup}s 
	 */
	public List<ProductTypeGroup> getProductTypeGroups(Set<ProductTypeGroupID> productTypeGroupIDs, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor progressMonitor)
	{
		// TODO: Implement authority checking
		return getJDOObjects(null, productTypeGroupIDs, fetchGroups, maxFetchDepth, progressMonitor);
	}
	
	/**
	 * Returns the {@link ProductTypeGroup} with the given {@link ProductTypeGroupID}.
	 * 
	 * @param productTypeGroupID
	 * @param fetchGroups the JDO fetch groups
	 * @param maxFetchDepth the maximum fetch depth
	 * @param progressMonitor the {@link ProgressMonitor} for displaying the progress
	 * @return the {@link ProductTypeGroup} with the given {@link ProductTypeGroupID}
	 */
	public ProductTypeGroup getProductTypeGroup(ProductTypeGroupID productTypeGroupID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor progressMonitor)
	{
		// TODO: Implement authority checking		
		return getJDOObject(null, productTypeGroupID, fetchGroups, maxFetchDepth, progressMonitor);
	}
}
