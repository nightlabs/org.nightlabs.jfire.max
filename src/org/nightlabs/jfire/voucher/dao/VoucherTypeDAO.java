package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class VoucherTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, VoucherType>
implements IJDOObjectDAO<VoucherType>
{
	private static VoucherTypeDAO sharedInstance = null;

	public static synchronized VoucherTypeDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new VoucherTypeDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<VoucherType> retrieveJDOObjects(
			Set<ProductTypeID> voucherTypeIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading VoucherTypes", 1);
		try {
//			VoucherManager vm = voucherManager;
//			if (vm == null)
//				vm = JFireEjbFactory.getBean(VoucherManager.class, SecurityReflector.getInitialContextProperties());
//
//			return vm.getVoucherTypes(voucherTypeIDs, fetchGroups, maxFetchDepth);
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return CollectionUtil.castCollection(sm.getProductTypes(voucherTypeIDs, fetchGroups, maxFetchDepth));
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	private VoucherManagerRemote voucherManager;

	@SuppressWarnings("unchecked")
	public synchronized List<VoucherType> getChildVoucherTypes(ProductTypeID parentVoucherTypeID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			voucherManager = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
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

	@Override
	public VoucherType storeJDOObject(VoucherType jdoObject, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			VoucherManagerRemote vm = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return vm.storeVoucherType(jdoObject, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the events that share the given ticket layout.
	 *
	 * @see Event#getEventIdsByTicketLayoutId(javax.jdo.PersistenceManager, TicketLayoutID)
	 */
	public List<VoucherType> getVoucherTypesByVoucherLayoutId(VoucherLayoutID voucherLayoutId, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			VoucherManagerRemote voucherManager = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<ProductTypeID> voucherTypeIds = voucherManager.getVoucherTypeIdsByVoucherLayoutId(voucherLayoutId);
			return getJDOObjects(null, voucherTypeIds, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
