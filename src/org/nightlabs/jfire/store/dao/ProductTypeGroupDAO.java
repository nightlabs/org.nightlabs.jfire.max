/**
 *
 */
package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;
import org.nightlabs.jfire.store.ProductTypeGroupIDSearchResult;
import org.nightlabs.jfire.store.ProductTypeGroupSearchResult;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class ProductTypeGroupDAO
extends BaseJDOObjectDAO<ProductTypeGroupID, ProductTypeGroup>
{
	private static final Logger logger = Logger.getLogger(ProductTypeGroupDAO.class);

	protected ProductTypeGroupDAO() {}

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
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ProductTypeGroup> retrieveJDOObjects(Set<ProductTypeGroupID> productTypeGroupIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Loading ProductTypeGroups", 1);
		try {
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return sm.getProductTypeGroups(productTypeGroupIDs, fetchGroups, maxFetchDepth);
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
		return getJDOObject(null, productTypeGroupID, fetchGroups, maxFetchDepth, progressMonitor);
	}

//	/**
//	 * Returns a List of all ProductTypeGroups which match the criteria in the given QueryCollection.
//	 *
//	 * @param queryCollection the QueryCollection with the queries to search
//	 * @param fetchGroups the FetchGroups to search with
//	 * @param maxFetchDepth the maximum fetch depth
//	 * @param progressMonitor the ProgressMonitor to show the progress
//	 * @return a List of all ProductTypeGroups which match the criteria in the given QueryCollection
//	 * @throws Exception
//	 */
//	public synchronized List<ProductTypeGroup> getProductTypeGroups(QueryCollection<? extends AbstractProductTypeGroupQuery> queryCollection,
//			String[] fetchGroups, int maxFetchDepth, ProgressMonitor progressMonitor)throws Exception
//	{
//		progressMonitor.beginTask("Load ProductTypeGroups", 100);
//		StoreManagerRemote = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
//		try {
//			Set<ProductTypeGroupID> productTypeGroupIDs = storeManager.getProductTypeGroupIDs(queryCollection);
//			progressMonitor.worked(50);
//			List<ProductTypeGroup> productTypeGroups = getJDOObjects(null, productTypeGroupIDs, fetchGroups, maxFetchDepth, progressMonitor);
//			progressMonitor.worked(50);
//			return productTypeGroups;
//		} finally {
//			progressMonitor.done();
//			storeManager = null;
//		}
//	}

//	/**
//	 * Returns a List of all ProductTypeGroups which match the criteria in the given QueryCollection.
//	 *
//	 * @param queryCollection the QueryCollection with the queries to search
//	 * @param fetchGroups the FetchGroups to search with
//	 * @param maxFetchDepth the maximum fetch depth
//	 * @param progressMonitor the ProgressMonitor to show the progress
//	 * @return a List of all ProductTypeGroups which match the criteria in the given QueryCollection
//	 * @throws Exception
//	 */
//	public synchronized ProductTypeGroupIDSearchResult getProductTypeGroupSearchResult(QueryCollection<? extends AbstractProductTypeGroupQuery> queryCollection,
//			String[] fetchGroups, int maxFetchDepth, ProgressMonitor progressMonitor)throws Exception
//	{
//		progressMonitor.beginTask("Load ProductTypeGroups", 100);
//		StoreManagerRemote = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
//		try {
//			progressMonitor.worked(50);
//			ProductTypeGroupIDSearchResult result = storeManager.getProductTypeGroupSearchResult(queryCollection, fetchGroups, maxFetchDepth);
//			progressMonitor.worked(50);
//			return result;
//		} finally {
//			progressMonitor.done();
//			storeManager = null;
//		}
//	}

	private StoreManagerRemote storeManager = null;

//	/**
//	 * Returns a {@link ProductTypeGroupSearchResult} which contains all {@link ProductTypeGroup}s
//	 * and its contained {@link ProductType}s which match the search results of the given
//	 * {@link QueryCollection} of {@link AbstractProductTypeGroupQuery}
//	 *
//	 * @param queryCollection the QueryCollection with the queries to search
//	 * @param fetchGroups the FetchGroups to search with
//	 * @param maxFetchDepth the maximum fetch depth
//	 * @param progressMonitor the ProgressMonitor to show the progress
//	 * @return a {@link ProductTypeGroupSearchResult} which contains all {@link ProductTypeGroup}s
//	 * and its contained {@link ProductType}s which match the search results of the given
//	 * {@link QueryCollection} of {@link AbstractProductTypeGroupQuery}
//	 * @throws Exception if something went wrong
//	 */
//	public ProductTypeGroupSearchResult getProductTypeGroupSearchResult(QueryCollection<? extends AbstractProductTypeGroupQuery> queryCollection,
//			String[] fetchGroupsProductTypeGroup, int maxFetchDepthProductTypeGroup,
//			String[] fetchGroupsProductType, int maxFetchDepthProductType,
//			ProgressMonitor progressMonitor)
//	throws Exception
//	{
//		progressMonitor.beginTask("Load ProductTypeGroups", 200);
//		try {
//			ProductTypeGroupSearchResult searchResult = new ProductTypeGroupSearchResult();
//			List<ProductTypeGroup> productTypeGroups = null;
//			ProductTypeGroupIDSearchResult resultIDs = null;
//			Map<ProductTypeGroupID, ProductTypeGroup> productTypeGroupID2ProductTypeGroup = new HashMap<ProductTypeGroupID, ProductTypeGroup>();
//			synchronized (this) {
//				try {
//					storeManager = JFireEjb3Factory.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
//					resultIDs = storeManager.getProductTypeGroupSearchResult(queryCollection);
//					progressMonitor.worked(50);
//					Set<ProductTypeGroupID> productTypeGroupIDs = resultIDs.getProductTypesGroupIDs();
//					productTypeGroups = getProductTypeGroups(productTypeGroupIDs, fetchGroupsProductTypeGroup,
//							maxFetchDepthProductTypeGroup, new SubProgressMonitor(progressMonitor, 50));
//					for (ProductTypeGroup productTypeGroup : productTypeGroups) {
//						productTypeGroupID2ProductTypeGroup.put((ProductTypeGroupID)
//								JDOHelper.getObjectId(productTypeGroup), productTypeGroup);
//					}
//				} finally {
//					storeManager = null;
//				}
//			}
//			Map<ProductTypeID, ProductType> productTypeID2ProductType = new HashMap<ProductTypeID, ProductType>();
//			Set<ProductTypeID> allProductTypesIDs = resultIDs.getAllProductTypeIDs();
//			List<ProductType> productTypes = ProductTypeDAO.sharedInstance().getProductTypes(
//					allProductTypesIDs, fetchGroupsProductType, maxFetchDepthProductType,
//					new SubProgressMonitor(progressMonitor, 50));
//			for (ProductType productType : productTypes) {
//				productTypeID2ProductType.put((ProductTypeID)
//						JDOHelper.getObjectId(productType), productType);
//			}
//
//			if (productTypeGroups != null && resultIDs != null) {
//				for (Entry entry : resultIDs.getEntries()) {
//					ProductTypeGroup group = productTypeGroupID2ProductTypeGroup.get(entry.getProductTypeGroupID());
//					if (group == null) {
//						logger.warn("No group found with this id: " + entry.getProductTypeGroupID());
//						continue;
//					}
//					org.nightlabs.jfire.store.ProductTypeGroupSearchResult.Entry e = searchResult.addEntry(group);
//					List<ProductTypeID> productTypesIDs = entry.getProductTypeIDs();
//					for (ProductTypeID productTypeID : productTypesIDs) {
//						ProductType productType = productTypeID2ProductType.get(productTypeID);
//						e.addProductType(productType);
//					}
//				}
//			}
//			progressMonitor.worked(50);
//			return searchResult;
//		} finally {
//			progressMonitor.done();
//		}
//	}


	public ProductTypeGroupSearchResult getProductTypeGroupSearchResultForProductTypeQueries(QueryCollection<? extends AbstractProductTypeQuery> queryCollection,
			String[] fetchGroupsProductTypeGroup, int maxFetchDepthProductTypeGroup,
			String[] fetchGroupsProductType, int maxFetchDepthProductType,
			ProgressMonitor progressMonitor
	)
	{
		progressMonitor.beginTask("Load ProductTypeGroups", 200);
		try {
			ProductTypeGroupSearchResult searchResult = new ProductTypeGroupSearchResult();
			List<ProductTypeGroup> productTypeGroups = null;
			ProductTypeGroupIDSearchResult resultIDs = null;
			Map<ProductTypeGroupID, ProductTypeGroup> productTypeGroupID2ProductTypeGroup = new HashMap<ProductTypeGroupID, ProductTypeGroup>();
			synchronized (this) {
				try {
					storeManager = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
					resultIDs = storeManager.getProductTypeGroupIDSearchResultForProductTypeQueries(queryCollection);
					progressMonitor.worked(50);
					Set<ProductTypeGroupID> productTypeGroupIDs = resultIDs.getProductTypesGroupIDs();
					productTypeGroups = getProductTypeGroups(productTypeGroupIDs, fetchGroupsProductTypeGroup,
							maxFetchDepthProductTypeGroup, new SubProgressMonitor(progressMonitor, 50));
					for (ProductTypeGroup productTypeGroup : productTypeGroups) {
						productTypeGroupID2ProductTypeGroup.put((ProductTypeGroupID)
								JDOHelper.getObjectId(productTypeGroup), productTypeGroup);
					}
				} catch (RuntimeException x) {
					throw x;
				} catch (Exception x) {
					throw new RuntimeException(x);
				} finally {
					storeManager = null;
				}
			}
			Map<ProductTypeID, ProductType> productTypeID2ProductType = new HashMap<ProductTypeID, ProductType>();
			Set<ProductTypeID> allProductTypesIDs = resultIDs.getAllProductTypeIDs();
			List<ProductType> productTypes = ProductTypeDAO.sharedInstance().getProductTypes(
					allProductTypesIDs, fetchGroupsProductType, maxFetchDepthProductType,
					new SubProgressMonitor(progressMonitor, 50));
			for (ProductType productType : productTypes) {
				productTypeID2ProductType.put((ProductTypeID)JDOHelper.getObjectId(productType), productType);
			}

			if (productTypeGroups != null && resultIDs != null) {
				for (ProductTypeGroupIDSearchResult.Entry entry : resultIDs.getEntries()) {
					ProductTypeGroup group = productTypeGroupID2ProductTypeGroup.get(entry.getProductTypeGroupID());
					if (group == null) {
						logger.warn("No group found with this id: " + entry.getProductTypeGroupID());
						continue;
					}
					org.nightlabs.jfire.store.ProductTypeGroupSearchResult.Entry e = searchResult.addEntry(group);
					List<ProductTypeID> productTypesIDs = entry.getProductTypeIDs();
					for (ProductTypeID productTypeID : productTypesIDs) {
						ProductType productType = productTypeID2ProductType.get(productTypeID);

						if (productType == null) {
							logger.warn("No productType found with this id: " + productTypeID);
							continue;
						}

						e.addProductType(productType);
					}
				}
			}
			progressMonitor.worked(50);
			return searchResult;
		} finally {
			progressMonitor.done();
		}
	}

}
