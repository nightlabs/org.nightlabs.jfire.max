package org.nightlabs.jfire.dunning;

import java.util.Map;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

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
	private Map<DunningFeeType, Map<Currency, Account>> feeType2CurrencyAccountMap;
	
	public SimpleDunningMoneyFlowConfig(String organisationID, String dunningMoneyFlowConfigID, DunningConfig dunningConfig) {
		super(organisationID, dunningMoneyFlowConfigID, dunningConfig);
	}
	
	@Override
	public Account getAccount(DunningFee fee, boolean isReverseBooking) {
		DunningFeeType feeType = fee.getDunningFeeType();
		Map<Currency, Account> currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		
		return null;
	}
}