package org.nightlabs.jfire.voucher.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherPriceConfigDAO
extends BaseJDOObjectDAO<PriceConfigID, VoucherPriceConfig>
{
	private static VoucherPriceConfigDAO sharedInstance = null;

	public static VoucherPriceConfigDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (VoucherPriceConfigDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new VoucherPriceConfigDAO();
			}
		}
		return sharedInstance;
	}

	private VoucherManagerRemote voucherManager;

	@Override
	protected Collection<VoucherPriceConfig> retrieveJDOObjects(
			Set<PriceConfigID> objectIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Load VoucherPriceConfigs", 1);
		try {
			VoucherManagerRemote vm = voucherManager;
			if (vm == null) vm = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return vm.getVoucherPriceConfigs(objectIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
		}
	}

	public synchronized List<VoucherPriceConfig> getVoucherPriceConfigs(
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			voucherManager = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Set<PriceConfigID> priceConfigIDs = voucherManager.getVoucherPriceConfigIDs();
				return getJDOObjects(null, priceConfigIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				voucherManager = null;
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<VoucherPriceConfig> getVoucherPriceConfigs(
			Collection<PriceConfigID> priceConfigIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (priceConfigIDs == null)
			return getVoucherPriceConfigs(fetchGroups, maxFetchDepth, monitor);

		return getJDOObjects(null, priceConfigIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public VoucherPriceConfig getVoucherPriceConfig(
			PriceConfigID priceConfigID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		return getJDOObject(null, priceConfigID, fetchGroups, maxFetchDepth, monitor);
	}
}
