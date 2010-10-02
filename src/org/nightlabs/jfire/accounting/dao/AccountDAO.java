package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountSearchFilter;
import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.query.AccountQuery;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

public class AccountDAO
extends BaseJDOObjectDAO<AnchorID, Account>
{
	private static AccountDAO sharedInstance = null;

	public static AccountDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (AccountDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new AccountDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<Account> retrieveJDOObjects(Set<AnchorID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading Accounts", 1);
		try {
			AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			return am.getAccounts(objectIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public List<Account> getAccounts(AccountSearchFilter accountSearchFilter, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			Set<AnchorID> accountIDs = am.getAccountIDs(accountSearchFilter);
			return getJDOObjects(null, accountIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<Account> getAccountsForQueries(
		QueryCollection<? extends AccountQuery> queries,	String[] fetchGroups,
		int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			Set<AnchorID> accountIDs = am.getAccountIDs(queries);
			return getJDOObjects(null, accountIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<Account> getAccounts(Collection<AnchorID> accountIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, accountIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Account getAccount(AnchorID accountID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, accountID, fetchGroups, maxFetchDepth, monitor);
	}

	public Account storeAccount(Account account, boolean get, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Save Account", 1);
		try {
			AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			account = am.storeAccount(account, get, fetchGroups, maxFetchDepth);
			if (account != null)
				getCache().put(null, account, fetchGroups, maxFetchDepth);
			return account;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
}
