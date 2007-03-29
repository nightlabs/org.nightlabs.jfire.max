package org.nightlabs.jfire.voucher.scripting;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

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
	private static final String[] FETCH_GROUPS_CURRENCY = new String[] {
		FetchPlan.DEFAULT,
		FetchPlan.ALL // TODO clean up fetch groups!!!
	};
	
	public PreviewParameterValuesResult(VoucherType voucherType) 
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(voucherType);
		if (pm == null)
			throw new IllegalArgumentException("voucherType is currently not persistent!");

		int oldMaxFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
		Collection oldFetchGroups = pm.getFetchPlan().getGroups();
		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig) voucherType.getInnerPriceConfig();
			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig) voucherType.getPackagePriceConfig();
			
			pm.getFetchPlan().setGroups(FETCH_GROUPS_CURRENCY);
			if (voucherPriceConfig != null && voucherPriceConfig.getCurrencies() != null)
				this.currencies = pm.detachCopyAll(voucherPriceConfig.getCurrencies());
			else
				this.currencies = null;
		} finally {
			pm.getFetchPlan().setMaxFetchDepth(oldMaxFetchDepth);
			pm.getFetchPlan().setGroups(oldFetchGroups);
		}
	}
	
	private Collection<Currency> currencies = null;
	public Collection<Currency> getCurrencies() {
		return currencies;
	}
}
