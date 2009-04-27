package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery;
import org.nightlabs.jfire.accounting.query.MoneyTransferQuery;
import org.nightlabs.jfire.base.JFireEjb3Factory;
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

	@Override
	protected Collection<MoneyTransfer> retrieveJDOObjects(
			Set<TransferID> productTransferIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return am.getMoneyTransfers(productTransferIDs, fetchGroups, maxFetchDepth);
	}

	public List<MoneyTransfer> getMoneyTransfers(
			MoneyTransferIDQuery productTransferIDQuery,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			List<TransferID> transferIDs = am.getMoneyTransferIDs(productTransferIDQuery);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<MoneyTransfer> getMoneyTransfers(
			Collection<MoneyTransferQuery> productTransferQueries,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			List<TransferID> transferIDs = am.getMoneyTransferIDs(productTransferQueries);
			return getJDOObjects(null, transferIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
