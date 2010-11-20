package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.dunning.id.DunningMoneyFlowConfigID;
import org.nightlabs.jfire.organisation.Organisation;

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
		table="JFireDunning_DunningMoneyFlowConfig"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningMoneyFlowConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningMoneyFlowConfig.class);

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningMoneyFlowConfigID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningMoneyFlowConfig() { }

	public DunningMoneyFlowConfig(String organisationID, String dunningMoneyFlowConfigID, DunningConfig dunningConfig) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningMoneyFlowConfigID, "dunningMoneyFlowConfigID"); //$NON-NLS-1$

		this.organisationID = organisationID;
		this.dunningMoneyFlowConfigID = dunningMoneyFlowConfigID;
		this.dunningConfig = dunningConfig;
	}

	/**
	 * This method returns the account a specific DunningFeeType will be booked on.
	 *
	 * @param feeType
	 * @param currency
	 * @param isReverseBooking
	 * @return
	 */
	public abstract Account getAccount(DunningFee fee, boolean isReverseBooking);

	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dunningMoneyFlowConfigID == null) ? 0
						: dunningMoneyFlowConfigID.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunningMoneyFlowConfig other = (DunningMoneyFlowConfig) obj;
		if (dunningMoneyFlowConfigID == null) {
			if (other.dunningMoneyFlowConfigID != null)
				return false;
		} else if (!dunningMoneyFlowConfigID
				.equals(other.dunningMoneyFlowConfigID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	@Override
	public String toString() {
//		return "DunningMoneyFlowConfig [dunningMoneyFlowConfigID="
//				+ dunningMoneyFlowConfigID + ", organisationID="
//				+ organisationID + "]";
		return super.toString() + '[' + organisationID + ',' + dunningMoneyFlowConfigID + ']';
	}
}