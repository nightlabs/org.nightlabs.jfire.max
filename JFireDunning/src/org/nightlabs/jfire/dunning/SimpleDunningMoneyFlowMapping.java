package org.nightlabs.jfire.dunning;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.dunning.id.SimpleDunningMoneyFlowMappingID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.l10n.Currency;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * 
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@PersistenceCapable(
	objectIdClass=SimpleDunningMoneyFlowMappingID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_SimpleMoneyFlowMapping"
)
public class SimpleDunningMoneyFlowMapping
	implements CloneableWithContext
{
	@PrimaryKey
	private String organisationID;
	
	@PrimaryKey
	private long simpleMoneyFlowMappingID;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	private SimpleDunningMoneyFlowConfig moneyFlowConfig;

	private DunningFeeType feeType;
	
	@Persistent(table="JFireDunning_SimpleMoneyFlowMapping_currency2Account")
	@Join
	private Map<Currency, Account> currency2Account;
	
	@Persistent(table="JFireDunning_SimpleMoneyFlowMapping_currency2AccountReverse")
	@Join
	private Map<Currency, Account> currency2AccountReverse;

	/**
	 * Constructor used for instantiating a simple mapping for interests.
	 * 
	 * @param moneyFlowConfig the config this mapping belongs to.
	 */
	public SimpleDunningMoneyFlowMapping(SimpleDunningMoneyFlowConfig moneyFlowConfig)
	{
		assert moneyFlowConfig != null;
		this.organisationID = moneyFlowConfig.getOrganisationID();
		this.simpleMoneyFlowMappingID = IDGenerator.nextID(SimpleDunningMoneyFlowMapping.class);
		this.currency2Account = new HashMap<Currency, Account>();
		this.currency2AccountReverse = new HashMap<Currency, Account>();
		this.moneyFlowConfig = moneyFlowConfig;
	}
	
	/**
	 * @param feeType
	 */
	public SimpleDunningMoneyFlowMapping(SimpleDunningMoneyFlowConfig moneyFlowConfig, DunningFeeType feeType)
	{
		this(moneyFlowConfig.getOrganisationID(), feeType, null);
		this.moneyFlowConfig = moneyFlowConfig;
	}

	/**
	 * @param feeType
	 */
	public SimpleDunningMoneyFlowMapping(String organisationID, DunningFeeType feeType)
	{
		this(organisationID, feeType, null);
	}

	/**
	 * @param feeType
	 * @param currency2Account
	 */
	public SimpleDunningMoneyFlowMapping(String organisationID, DunningFeeType feeType, 
			Map<Currency, Account> currency2Account)
	{
		assert organisationID != null;
		assert feeType != null;
		this.organisationID = organisationID;
		this.simpleMoneyFlowMappingID = IDGenerator.nextID(SimpleDunningMoneyFlowMapping.class);
		this.feeType = feeType;
		this.currency2Account = currency2Account;
		if (currency2Account == null)
			currency2Account = new HashMap<Currency, Account>();
		
		this.currency2AccountReverse = new HashMap<Currency, Account>();
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getSimpleMoneyFlowMappingID()
	{
		return simpleMoneyFlowMappingID;
	}
	
	/**
	 * @return the moneyFlowConfig
	 */
	public SimpleDunningMoneyFlowConfig getMoneyFlowConfig()
	{
		return moneyFlowConfig;
	}

	public void addAccount(Currency currency, Account account, boolean isReverseBooking)
	{
		if (isReverseBooking)
			currency2AccountReverse.put(currency, account);
		else
			currency2Account.put(currency, account);
	}
	
	public Account getAccount(Currency currency, boolean isReverseBooking)
	{
		if (isReverseBooking)
			return currency2AccountReverse.get(currency);
		else
			return currency2Account.get(currency);
	}
	
	public void removeAccount(Currency currency, boolean isReverseBooking)
	{
		if (isReverseBooking)
			currency2AccountReverse.remove(currency);
		else
			currency2Account.remove(currency);
	}
	
	public Map<Currency, Account> getCurrency2Account()
	{
		return Collections.unmodifiableMap(currency2Account);
	}

	public Map<Currency, Account> getCurrency2AccountReverse()
	{
		return Collections.unmodifiableMap(currency2AccountReverse);
	}

	public DunningFeeType getFeeType()
	{
		return feeType;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dunning.clone.Cloneable#cloneCreate(org.nightlabs.jfire.dunning.clone.CloneContext, boolean)
	 */
	@Override
	public SimpleDunningMoneyFlowMapping clone(CloneContext context, boolean cloneReferences)
	{
		//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
		//	DunningMoneyFlowConfig clone = (DunningMoneyFlowConfig) super.clone();
		SimpleDunningMoneyFlowMapping clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
		//END OF WORKAROUND

		clone.simpleMoneyFlowMappingID = IDGenerator.nextID(SimpleDunningMoneyFlowMapping.class);
		// we do NOT clone these two references as the they are not integral parts of this object
		clone.feeType = feeType;
		clone.moneyFlowConfig = moneyFlowConfig;
		
		if (cloneReferences)
		{
			if(currency2Account != null)
				clone.currency2Account = new HashMap<Currency, Account>(currency2Account);
			else
				clone.currency2Account = new HashMap<Currency, Account>();
			
			if (currency2AccountReverse != null)
				clone.currency2AccountReverse = new HashMap<Currency, Account>(currency2AccountReverse);
			else
				clone.currency2AccountReverse = new HashMap<Currency, Account>();
		}
		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
		SimpleDunningMoneyFlowMapping clonedMapping = (SimpleDunningMoneyFlowMapping) clone;
		clonedMapping.feeType = context.getClone(feeType, true);
		clonedMapping.moneyFlowConfig = context.getClone(moneyFlowConfig, true);
	}	
}
