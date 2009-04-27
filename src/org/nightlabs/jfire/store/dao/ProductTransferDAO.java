package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.query.ProductTransferQuery;
import org.nightlabs.jfire.transfer.id.TransferID;
import org.nightlabs.progress.ProgressMonitor;

public class ProductTransferDAO
	extends BaseJDOObjectDAO<TransferID, ProductTransfer>
{
	private static ProductTransferDAO sharedInstance;

	public static synchronized ProductTransferDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ProductTransferDAO();

		return sharedInstance;
	}

	protected ProductTransferDAO() { }

	@Override
	protected Collection<ProductTransfer> retrieveJDOObjects(
			Set<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return sm.getProductTransfers(productTransferIDs, fetchGroups, maxFetchDepth);
	}

//	@SuppressWarnings("unchecked")
//	public List<ProductTransfer> getProductTransfers(
//			ProductTransferIDQuery productTransferIDQuery,
//			String[] fetchGroups, int maxFetchDepth,
//			ProgressMonitor monitor)
//	{
//		try {
//			StoreManagerRemote = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
//			List<TransferID> transferIDs = sm.getProductTransferIDs(productTransferIDQuery);
//			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

	public List<ProductTransfer> getProductTransfers(
			QueryCollection<? extends ProductTransferQuery> productTransferQueries,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			List<TransferID> transferIDs = sm.getProductTransferIDs(productTransferQueries);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//	public List<ProductTransfer> getProductTransfersByIDQueries(
//		QueryCollection<ProductTransferIDQuery> queries,
//		String[] fetchGroups, int maxFetchDepth,
//		ProgressMonitor monitor)
//	{
//		try
//		{
//			StoreManagerRemote = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
//			List<TransferID> transferIDs = sm.getProductTransferIDs(queries);
//			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
//		}
//		catch (Exception e)
//		{
//			throw new RuntimeException("Problem fetching ProductTransfers:", e);
//		}
//	}
}
