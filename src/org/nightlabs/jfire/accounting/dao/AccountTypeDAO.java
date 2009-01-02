package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class AccountTypeDAO
extends BaseJDOObjectDAO<AccountTypeID, AccountType>
{
	private static AccountTypeDAO sharedInstance = null;

	public static AccountTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (AccountTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new AccountTypeDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Collection<AccountType> retrieveJDOObjects(Set<AccountTypeID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading AccountTypes", 1);
		try {
			AccountingManager am = JFireEjbUtil.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
			return am.getAccountTypes(objectIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	public List<AccountType> getAccountTypes(String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManager am = JFireEjbUtil.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
			Set<AccountTypeID> accountTypeIDs = am.getAccountTypeIDs();
			return getJDOObjects(null, accountTypeIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<AccountType> getAccountTypes(Collection<AccountTypeID> accountTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, accountTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public AccountType getAccountType(AccountTypeID accountTypeID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, accountTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
