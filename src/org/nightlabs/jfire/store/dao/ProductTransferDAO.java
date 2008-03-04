package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.query.ProductTransferIDQuery;
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

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ProductTransfer> retrieveJDOObjects(
			Set<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return sm.getProductTransfers(productTransferIDs, fetchGroups, maxFetchDepth);
	}

	@SuppressWarnings("unchecked")
	public List<ProductTransfer> getProductTransfers(
			ProductTransferIDQuery productTransferIDQuery,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<TransferID> transferIDs = sm.getProductTransferIDs(productTransferIDQuery);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ProductTransfer> getProductTransfers(
			QueryCollection<ProductTransfer, ProductTransferQuery> productTransferQueries,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<TransferID> transferIDs = sm.getProductTransferIDs(productTransferQueries);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<ProductTransfer> getProductTransfersByIDQueries(
		QueryCollection<TransferID, ProductTransferIDQuery> queries,
		String[] fetchGroups, int maxFetchDepth,
		ProgressMonitor monitor)
	{
		try
		{
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<TransferID> transferIDs = sm.getProductTransferIDs(queries);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Problem fetching ProductTransfers:", e);
		}
	}
}
