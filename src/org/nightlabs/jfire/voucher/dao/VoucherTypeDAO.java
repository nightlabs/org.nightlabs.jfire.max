package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.VoucherManager;
import org.nightlabs.jfire.voucher.VoucherManagerUtil;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, VoucherType>
implements IJDOObjectDAO<VoucherType>
{
	private static VoucherTypeDAO sharedInstance = null;

	public static VoucherTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (VoucherTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new VoucherTypeDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<VoucherType> retrieveJDOObjects(
			Set<ProductTypeID> voucherTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading VoucherTypes", 1);
		try {
			VoucherManager vm = voucherManager;
			if (vm == null)
				vm = VoucherManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return vm.getVoucherTypes(voucherTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	private VoucherManager voucherManager;

	@SuppressWarnings("unchecked")
	public synchronized List<VoucherType> getChildVoucherTypes(ProductTypeID parentVoucherTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			voucherManager = VoucherManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			try {
				Collection<ProductTypeID> voucherTypeIDs = voucherManager.getChildVoucherTypeIDs(parentVoucherTypeID);
				return getJDOObjects(null, voucherTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				voucherManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<VoucherType> getVoucherTypes(Collection<ProductTypeID> voucherTypeIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (voucherTypeIDs == null)
			throw new IllegalArgumentException("voucherTypeIDs must not be null!");

		return getJDOObjects(null, voucherTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public VoucherType getVoucherType(ProductTypeID voucherTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (voucherTypeID == null)
			throw new IllegalArgumentException("voucherTypeID must not be null!");

		return getJDOObject(null, voucherTypeID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.jdo.IJDOObjectDAO#storeJDOObject(java.lang.Object, boolean, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Implement
	public VoucherType storeJDOObject(VoucherType jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			VoucherManager vm = VoucherManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return vm.storeVoucherType(jdoObject, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
