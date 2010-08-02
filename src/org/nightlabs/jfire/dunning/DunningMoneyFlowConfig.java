package org.nightlabs.jfire.dunning;

import java.util.Map;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Currency;

public class DunningMoneyFlowConfig 
{
	private Map<Currency, Account> currency2InterestAccount;
	public Account getAccount(DunningFeeType feeType, Currency currency, boolean isReverseBooking) {
		return null;
	}
}
