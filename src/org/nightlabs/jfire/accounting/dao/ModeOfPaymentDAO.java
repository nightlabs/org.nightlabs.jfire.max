package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class ModeOfPaymentDAO
		extends BaseJDOObjectDAO<ModeOfPaymentID, ModeOfPayment>
{
	private static ModeOfPaymentDAO sharedInstance = null;

	public synchronized static ModeOfPaymentDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new ModeOfPaymentDAO();

		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ModeOfPayment> retrieveJDOObjects(
			Set<ModeOfPaymentID> modeOfPaymentIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		AccountingManager am = accountingManager;
		if (am == null)
			am = JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());

		return am.getModeOfPayments(modeOfPaymentIDs, fetchGroups, maxFetchDepth);
	}

	private AccountingManager accountingManager;

	@SuppressWarnings("unchecked")
	public synchronized List<ModeOfPayment> getModeOfPayments(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			accountingManager = JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<ModeOfPaymentID> modeOfPaymentIDs = accountingManager.getAllModeOfPaymentIDs();
				return getJDOObjects(null, modeOfPaymentIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				accountingManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<ModeOfPayment> getModeOfPayments(Set<ModeOfPaymentID> modeOfPaymentIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, modeOfPaymentIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
