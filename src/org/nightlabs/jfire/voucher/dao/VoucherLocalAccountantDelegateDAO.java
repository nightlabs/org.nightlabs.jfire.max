package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherLocalAccountantDelegateDAO
extends BaseJDOObjectDAO<LocalAccountantDelegateID, VoucherLocalAccountantDelegate>
{
	private static VoucherLocalAccountantDelegateDAO sharedInstance = null;

	public static VoucherLocalAccountantDelegateDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (VoucherLocalAccountantDelegateDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new VoucherLocalAccountantDelegateDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<VoucherLocalAccountantDelegate> retrieveJDOObjects(
			Set<LocalAccountantDelegateID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		VoucherManagerRemote vm = voucherManager;
		if (vm == null) vm = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return vm.getVoucherLocalAccountantDelegates(objectIDs, fetchGroups, maxFetchDepth);
	}

	private VoucherManagerRemote voucherManager;

	public synchronized List<VoucherLocalAccountantDelegate> getVoucherLocalAccountantDelegates(
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			voucherManager = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<LocalAccountantDelegateID> ids = voucherManager.getVoucherLocalAccountantDelegateIDs();
				return getJDOObjects(null, ids, fetchGroups, maxFetchDepth, monitor);
			} finally {
				voucherManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
