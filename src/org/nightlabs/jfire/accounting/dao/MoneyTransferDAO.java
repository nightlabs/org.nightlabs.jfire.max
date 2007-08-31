package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery;
import org.nightlabs.jfire.accounting.query.MoneyTransferQuery;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.transfer.id.TransferID;
import org.nightlabs.progress.ProgressMonitor;

public class MoneyTransferDAO
extends BaseJDOObjectDAO<TransferID, MoneyTransfer>
{
	private static MoneyTransferDAO sharedInstance;

	public static synchronized MoneyTransferDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new MoneyTransferDAO();

		return sharedInstance;
	}

	protected MoneyTransferDAO() { }

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<MoneyTransfer> retrieveJDOObjects(
			Set<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManager sm = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return sm.getMoneyTransfers(productTransferIDs, fetchGroups, maxFetchDepth);
	}

	@SuppressWarnings("unchecked")
	public List<MoneyTransfer> getMoneyTransfers(
			MoneyTransferIDQuery productTransferIDQuery,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			AccountingManager sm = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<TransferID> transferIDs = sm.getMoneyTransferIDs(productTransferIDQuery);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<MoneyTransfer> getMoneyTransfers(
			Collection<MoneyTransferQuery> productTransferQueries,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			AccountingManager sm = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<TransferID> transferIDs = sm.getMoneyTransferIDs(productTransferQueries);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
