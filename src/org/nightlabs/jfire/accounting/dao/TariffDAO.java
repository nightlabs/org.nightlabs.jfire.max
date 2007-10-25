package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class TariffDAO
		extends BaseJDOObjectDAO<TariffID, Tariff>
{
	private static TariffDAO sharedInstance = null;

	public static TariffDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (TariffDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new TariffDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	@Implement
	protected Collection<Tariff> retrieveJDOObjects(
			Set<TariffID> tariffIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManager am = accountingManager;
		if (am == null)
			am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

		return am.getTariffs(tariffIDs, fetchGroups, maxFetchDepth);
	}

	private AccountingManager accountingManager;

	public List<Tariff> getTariffs(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getTariffs(null, false, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * @param organisationID <code>null</code> in order to get all tariffs (no filtering). non-<code>null</code> to filter by <code>organisationID</code>.
	 * @param inverse This applies only if <code>organisationID != null</code>. If <code>true</code>, it will return all {@link TariffID}s where the <code>organisationID</code>
	 *		is NOT the one passed as parameter <code>organisationID</code>.
	 * @param fetchGroups
	 * @param maxFetchDepth
	 * @param monitor
	 * @return
	 */
	public synchronized List<Tariff> getTariffs(String organisationID, boolean inverse, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			accountingManager = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Set<TariffID> tariffIDs = accountingManager.getTariffIDs(organisationID, inverse);
				return getJDOObjects(null, tariffIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				accountingManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<Tariff> getTariffs(Set<TariffID> tariffIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, tariffIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
