package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherLayoutDAO
extends BaseJDOObjectDAO<VoucherLayoutID, VoucherLayout>
implements IJDOObjectDAO<VoucherLayout>
{

	private static VoucherLayoutDAO sharedInstance;

	protected VoucherLayoutDAO() {
		super();
	}

	public static VoucherLayoutDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (VoucherLayoutDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new VoucherLayoutDAO();
			}
		}

		return sharedInstance;
	}

	@Override
	protected Collection<VoucherLayout> retrieveJDOObjects(Set<VoucherLayoutID> objectIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception {

		monitor.beginTask("Loading VoucherLayouts", 3);
		try {
			VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
			monitor.worked(1);
			List<VoucherLayout> VoucherLayouts = voucherManager.getVoucherLayouts(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(2);

			return VoucherLayouts;
		} finally {
			monitor.done();
		}
	}

	public VoucherLayout getVoucherLayout(VoucherLayoutID VoucherLayoutID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, VoucherLayoutID, fetchGroups, maxFetchDepth, monitor);
	}

	public Collection<VoucherLayout> getVoucherLayouts(Set<VoucherLayoutID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, objectIDs, fetchGroups, maxFetchDepth, monitor);
	}

// TODO Implement the following methods for voucher layouts (copied from TicketLayoutDAO and replaced TicketLayout by VoucherLayout yet). Tobias.

	public List<VoucherLayout> getAllVoucherLayouts(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
		return getJDOObjects(null, voucherManager.getAllVoucherLayoutIds(), fetchGroups, maxFetchDepth, monitor);
	}

	@Override
	public VoucherLayout storeJDOObject(VoucherLayout jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
		return voucherManager.storeVoucherLayout(jdoObject, get, fetchGroups, maxFetchDepth);
	}

	public void deleteVoucherLayout(VoucherLayout VoucherLayout) {
		VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
		voucherManager.deleteVoucherLayout((VoucherLayoutID) JDOHelper.getObjectId(VoucherLayout));
	}

	/**
	 * Replaces the persistent oldVoucherLayout by the <b>non-persistent</b> newVoucherLayout by first replacing every reference of the old one
	 * by the new one and then deleting the old voucher layout.
	 *
	 * @param oldVoucherLayout
	 * @param newVoucherLayout
	 */
	public void replaceVoucherLayout(VoucherLayoutID oldVoucherLayoutId, VoucherLayout newVoucherLayout) {
		VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
		voucherManager.replaceVoucherLayout(oldVoucherLayoutId, newVoucherLayout);
	}

	public Collection<VoucherLayoutID> getVoucherLayoutIdsByFileName(String fileName) {
		VoucherManagerRemote voucherManager = getEjbProvider().getRemoteBean(VoucherManagerRemote.class);
		return voucherManager.getVoucherLayoutIdsByFileName(fileName);
	}
}
