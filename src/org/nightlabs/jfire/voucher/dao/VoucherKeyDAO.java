package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.voucher.VoucherManager;
import org.nightlabs.jfire.voucher.VoucherManagerUtil;
import org.nightlabs.jfire.voucher.store.VoucherKey;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherKeyDAO
extends BaseJDOObjectDAO<VoucherKeyID, VoucherKey>
{
	private static VoucherKeyDAO sharedInstance = null;

	public static VoucherKeyDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (VoucherKeyDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new VoucherKeyDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<VoucherKey> retrieveJDOObjects(
			Set<VoucherKeyID> voucherKeyIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading VoucherKeys", 1);
		try {
			VoucherManager vm = voucherManager;
			if (vm == null) vm = VoucherManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return vm.getVoucherKeys(voucherKeyIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private VoucherManager voucherManager;

	public List<VoucherKey> getVoucherKeys(
			Collection<VoucherKeyID> voucherKeyIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, voucherKeyIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * @param voucherKeyString The key string (e.g. "v1F3dkjf3aX3")
	 * @param fetchGroups The fetch-groups used for detaching.
	 * @param maxFetchDepth The fetch-depth used for detaching.
	 * @param monitor A progress monitor.
	 * @return Either <code>null</code>, if there is no VoucherKey for the given <code>voucherKeyString</code> or
	 *		the requested instance of {@link VoucherKey}.
	 */
	public synchronized VoucherKey getVoucherKey(
			String voucherKeyString, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		try {
			voucherManager = VoucherManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				VoucherKeyID voucherKeyID = voucherManager.getVoucherKeyID(voucherKeyString);
				if (voucherKeyID == null)
					return null;

				return getJDOObject(null, voucherKeyID, fetchGroups, maxFetchDepth, monitor);
			} finally {
				voucherManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
