/**
 *
 */
package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class PriceFragmentTypeDAO
extends BaseJDOObjectDAO<PriceFragmentTypeID, PriceFragmentType>
{
	private static PriceFragmentTypeDAO sharedInstance;

	public static PriceFragmentTypeDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new PriceFragmentTypeDAO();
		return sharedInstance;
	}

	private PriceFragmentTypeDAO() {
	}

	@Override
	protected Collection<PriceFragmentType> retrieveJDOObjects(Set<PriceFragmentTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Loading Accounts", 1);
		try {
			AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return am.getPriceFragmentTypes(objectIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public Collection<PriceFragmentType> getPriceFragmentTypes(String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<PriceFragmentTypeID> priceFragementTypeIDs = am.getPriceFragmentTypeIDs();
			return getJDOObjects(null, priceFragementTypeIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException(e);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public PriceFragmentType getPriceFragmentType(PriceFragmentTypeID priceFragmentTypeID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, priceFragmentTypeID, fetchGroups, maxFetchDepth, monitor);
	}

}
