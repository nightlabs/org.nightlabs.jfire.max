package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.progress.ProgressMonitor;

public class ModeOfPaymentFlavourDAO
		extends BaseJDOObjectDAO<ModeOfPaymentFlavourID, ModeOfPaymentFlavour>
{
	private static ModeOfPaymentFlavourDAO sharedInstance = null;

	public synchronized static ModeOfPaymentFlavourDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ModeOfPaymentFlavourDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<ModeOfPaymentFlavour> retrieveJDOObjects(
			Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManagerRemote am = accountingManager;
		if (am == null)
			am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);

		return am.getModeOfPaymentFlavours(modeOfPaymentFlavourIDs, fetchGroups, maxFetchDepth);
	}

	private AccountingManagerRemote accountingManager;

	public synchronized List<ModeOfPaymentFlavour> getModeOfPaymentFlavours(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			accountingManager = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			try {
				Set<ModeOfPaymentFlavourID> tariffIDs = accountingManager.getAllModeOfPaymentFlavourIDs();
				return getJDOObjects(null, tariffIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				accountingManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<ModeOfPaymentFlavour> getModeOfPaymentFlavours(Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, modeOfPaymentFlavourIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
