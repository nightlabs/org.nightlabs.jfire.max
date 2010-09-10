package org.nightlabs.jfire.dunning;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.l10n.Currency;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_SimpleDunningMoneyFlowConfig"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class SimpleDunningMoneyFlowConfig 
extends DunningMoneyFlowConfig 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SimpleDunningMoneyFlowConfig.class);
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleDunningMoneyFlowConfig() { }
	
	@Join
	@Persistent(table="JFireDunning_SimpleDunningMoneyFlowConfig_feeType2CurrencyAccountMap")
	private Map<DunningFeeType, Map<Currency, Account>> feeType2CurrencyAccountMap;
	
	public SimpleDunningMoneyFlowConfig(String organisationID, String dunningMoneyFlowConfigID, DunningConfig dunningConfig) {
		super(organisationID, dunningMoneyFlowConfigID, dunningConfig);
		feeType2CurrencyAccountMap = new HashMap<DunningFeeType, Map<Currency,Account>>();
	}
	
	@Override
	public Account getAccount(DunningFee fee, boolean isReverseBooking) {
		DunningProcess dunningProcess = fee.getDunningLetter().getDunningProcess();
		Currency currency = dunningProcess.getCurrency();
		
		DunningFeeType feeType = fee.getDunningFeeType();
		Map<Currency, Account> currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		
		return currency2AccountMap.get(currency);
	}
	
	public Map<DunningFeeType, Map<Currency, Account>> getFeeType2CurrencyAccountMap() {
		return feeType2CurrencyAccountMap;
	}
	
	public void addAccount(DunningFeeType feeType, Currency currency, Account account) {
		Map<Currency, Account> currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		if (currency2AccountMap == null) {
			currency2AccountMap = new HashMap<Currency, Account>();
		}
		currency2AccountMap.put(currency, account);
	}
	
	public void removeAccount(DunningFeeType feeType, Currency currency) {
		Map<Currency, Account> currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		if (currency2AccountMap == null) {
			return;
		}
		currency2AccountMap.remove(currency);
	}
}