package org.nightlabs.jfire.voucher.scripting;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class PreviewParameterValuesResult
implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] FETCH_GROUPS_CURRENCY = new String[] {
		FetchPlan.DEFAULT,
	};
	
	public PreviewParameterValuesResult(VoucherType voucherType)
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(voucherType);
		if (pm == null)
			throw new IllegalArgumentException("voucherType is currently not persistent!");

		FetchPlanBackup fetchPlanBackup = pm == null ? null : NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(FETCH_GROUPS_CURRENCY);
			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig) voucherType.getPackagePriceConfig();
			if (voucherPriceConfig != null && voucherPriceConfig.getCurrencies() != null)
				this.currencies = pm.detachCopyAll(voucherPriceConfig.getCurrencies());
			else
				this.currencies = null;
		} finally {
			if (fetchPlanBackup != null && pm != null)
				NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
		}
	}
	
	private Collection<Currency> currencies = null;
	public Collection<Currency> getCurrencies() {
		return currencies;
	}
}
