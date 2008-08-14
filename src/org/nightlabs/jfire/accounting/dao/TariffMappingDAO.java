package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.TariffMapping;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.id.TariffMappingID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class TariffMappingDAO
		extends BaseJDOObjectDAO<TariffMappingID, TariffMapping>
{
	private static TariffMappingDAO _sharedInstance = null;

	public static synchronized TariffMappingDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new TariffMappingDAO();

		return _sharedInstance;
	}

	protected TariffMappingDAO() { }

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<TariffMapping> retrieveJDOObjects(
			Set<TariffMappingID> tariffMappingIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManager am = accountingManager;
		if (am == null)
			am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

		return am.getTariffMappings(tariffMappingIDs, fetchGroups, maxFetchDepth);
	}

	private AccountingManager accountingManager;

	@SuppressWarnings("unchecked")
	public synchronized List<TariffMapping> getTariffMappings(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			accountingManager = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Set<TariffMappingID> tariffMappingIDs = accountingManager.getTariffMappingIDs();
				return getJDOObjects(null, tariffMappingIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				accountingManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<TariffMapping> getTariffMappings(Set<TariffMappingID> tariffMappingIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, tariffMappingIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public TariffMapping createTariffMapping(TariffID localTariffID, TariffID partnerTariffID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManager am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			TariffMapping tm = am.createTariffMapping(localTariffID, partnerTariffID, get, fetchGroups, maxFetchDepth);

			if (tm != null)
				Cache.sharedInstance().put(null, tm, fetchGroups, maxFetchDepth);

			return tm;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
