package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.dunning.id.DunningMoneyFlowConfigID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.l10n.Currency;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * The abstract base class for all implementations that map a triple consisting
 * of the DunningFeeType, the currency and the direction of the booking to an
 * account it shall be booked to.<br>
 *
 * <br>A simple implementation is the SimpleDunningMoneyFlowConfig that directly maps
 * such a triple to an account via hash maps.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningMoneyFlowConfigID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_MoneyFlowConfig"
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningMoneyFlowConfig
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long moneyFlowConfigID;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningMoneyFlowConfig() { }

	public DunningMoneyFlowConfig(String organisationID, long moneyFlowConfigID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.moneyFlowConfigID = moneyFlowConfigID;
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	public long getMoneyFlowConfigID()
	{
		return moneyFlowConfigID;
	}

	/**
	 * This method returns the account a specific DunningFeeType will be booked on.
	 * @param fee
	 * @param currency TODO
	 * @param isReverseBooking
	 *
	 * @return
	 */
	public abstract Account getAccount(DunningFee fee, Currency currency, boolean isReverseBooking);
	
	public abstract Account getAccount(DunningFeeType feeType, Currency currency, boolean isReverseBooking);
	
	public abstract Account getInterestAccount(Currency currency, boolean isReverseBooking);

	@Override
	public DunningMoneyFlowConfig clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//		DunningMoneyFlowConfig clone = (DunningMoneyFlowConfig) super.clone();
		DunningMoneyFlowConfig clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND
		
		clone.moneyFlowConfigID = IDGenerator.nextID(DunningMoneyFlowConfig.class);
		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ (int) (moneyFlowConfigID ^ (moneyFlowConfigID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningMoneyFlowConfig other = (DunningMoneyFlowConfig) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(moneyFlowConfigID, other.moneyFlowConfigID))
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DunningMoneyFlowConfig [organisationID=" + organisationID + ", dunningMoneyFlowConfigID="
				+ moneyFlowConfigID + "]";
	}
}