package org.nightlabs.jfire.dunning;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.l10n.Currency;

/**
 * 
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class SimpleDunningMoneyFlowConfig 
	extends DunningMoneyFlowConfig 
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleDunningMoneyFlowConfig() { }
	
	@Join
	@Key(mappedBy="feeType")
	@Persistent(
			table="JFireDunning_SimpleMoneyFlowConfig_feeType2CurrencyAccountMap",
			mappedBy="moneyFlowConfig",
			dependentValue="true")
	private Map<DunningFeeType, SimpleDunningMoneyFlowMapping> feeType2CurrencyAccountMap;

	@Persistent(dependent="true")
	private SimpleDunningMoneyFlowMapping interestMapping;
	
//	private Map<Currency, Account> currency2InterestAccount;
//	private Map<Currency, Account> currency2ReverseInterestAccount;

	public SimpleDunningMoneyFlowConfig(String organisationID, long dunningMoneyFlowConfigID, DunningConfig dunningConfig)
	{
		super(organisationID, dunningMoneyFlowConfigID);
		feeType2CurrencyAccountMap = new HashMap<DunningFeeType, SimpleDunningMoneyFlowMapping>();
		interestMapping = new SimpleDunningMoneyFlowMapping(this, null);
//		currency2InterestAccount = new HashMap<Currency, Account>();
//		currency2ReverseInterestAccount = new HashMap<Currency, Account>();
	}

	@Override
	public Account getAccount(DunningFee fee, Currency currency, boolean isReverseBooking)
	{
		assert fee != null : "fee must NOT be null!";
		return getAccount(fee.getDunningFeeType(), currency, isReverseBooking);
	}

	@Override
	public Account getAccount(DunningFeeType feeType, Currency currency, boolean isReverseBooking)
	{
		assert currency != null : "currency must NOT be null!";
		assert feeType != null : "feeType must NOT be null!";
		
		SimpleDunningMoneyFlowMapping currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		
		if (currency2AccountMap == null)
			return null;
		
		return currency2AccountMap.getAccount(currency, isReverseBooking);
	}

	@Override
	public Account getInterestAccount(Currency currency, boolean isReverseBooking)
	{
		return interestMapping.getAccount(currency, isReverseBooking);
	}
	
	public void setInterestAccount(Currency currency, boolean isReverseBooking, Account targetAccount)
	{
		interestMapping.addAccount(currency, targetAccount, isReverseBooking);
	}
	
	public Map<DunningFeeType, SimpleDunningMoneyFlowMapping> getFeeType2CurrencyAccountMap()
	{
		return Collections.unmodifiableMap(feeType2CurrencyAccountMap);
	}
	
	public void addAccount(DunningFeeType feeType, Currency currency, Account account, boolean isReverseBooking)
	{
		assert feeType != null;
		assert currency != null;
		assert account != null;
		
		SimpleDunningMoneyFlowMapping currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		if (currency2AccountMap == null)
		{
			currency2AccountMap = new SimpleDunningMoneyFlowMapping(getOrganisationID(), feeType);
			feeType2CurrencyAccountMap.put(feeType, currency2AccountMap);
		}
		
		currency2AccountMap.addAccount(currency, account, isReverseBooking);
	}
	
	public void removeAccount(DunningFeeType feeType, Currency currency, boolean isReverseBooking)
	{
		SimpleDunningMoneyFlowMapping currency2AccountMap = feeType2CurrencyAccountMap.get(feeType);
		if (currency2AccountMap == null)
			return;

		currency2AccountMap.removeAccount(currency, isReverseBooking);
	}
	
	@Override
	public SimpleDunningMoneyFlowConfig clone(CloneContext context, boolean cloneReferences)
	{
		SimpleDunningMoneyFlowConfig clone = (SimpleDunningMoneyFlowConfig) super.clone(context, cloneReferences);
		if (feeType2CurrencyAccountMap != null)
		{
			clone.feeType2CurrencyAccountMap = 
				new HashMap<DunningFeeType, SimpleDunningMoneyFlowMapping>(feeType2CurrencyAccountMap.size());
		}
		else
		{
			clone.feeType2CurrencyAccountMap = new HashMap<DunningFeeType, SimpleDunningMoneyFlowMapping>();
		}

		if (cloneReferences)
		{
			if (feeType2CurrencyAccountMap != null)
			{
				for (SimpleDunningMoneyFlowMapping mapping : feeType2CurrencyAccountMap.values())
				{
					context.createClone(mapping);
				}
			}
		}
		
		return clone;
	}
	
	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
		super.updateReferencesOfClone(clone, context);

		SimpleDunningMoneyFlowConfig config = (SimpleDunningMoneyFlowConfig) clone;
		if (feeType2CurrencyAccountMap != null)
		{
			for (Entry<DunningFeeType, SimpleDunningMoneyFlowMapping> feeType2CurrencyAccountMapEntry : 
				feeType2CurrencyAccountMap.entrySet())
			{
				// When this cloned instance was created, it still pointed to the original DunningFeeType
				// this DunningFeeType is now replaced by the cloned instance. 
				SimpleDunningMoneyFlowMapping originalCurrency2Account = feeType2CurrencyAccountMapEntry.getValue();

				// the cloned mapping has its references updated before we do, hence the FeeType is already updated.
				SimpleDunningMoneyFlowMapping clonedMapping = context.getClone(originalCurrency2Account);
				config.feeType2CurrencyAccountMap.put(clonedMapping.getFeeType(), clonedMapping);
			}		
		}
	}
	
}