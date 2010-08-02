package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.dunning.id.DunningInterestID;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningInterestID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningInterest")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningInterest 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningInterestID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetterEntry dunningLetterEntry;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningInterest backReference;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date creditPeriodFromIncl;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date creditPeriodToExcl;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long baseAmount;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private BigDecimal interestPercentage;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long interestAmount;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaid;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountToPay;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paidDT;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningInterest() { }
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningInterestID() {
		return dunningInterestID;
	}
	
	public DunningLetterEntry getDunningLetterEntry() {
		return dunningLetterEntry;
	}
	
	public Date getCreditPeriodFromIncl() {
		return creditPeriodFromIncl;
	}
	
	public Date getCreditPeriodToExcl() {
		return creditPeriodToExcl;
	}
	
	public Currency getCurrency() {
		return currency;
	}
	
	public long getBaseAmount() {
		return baseAmount;
	}
	
	public BigDecimal getInterestPercentage() {
		return interestPercentage;
	}
	
	public long getInterestAmount() {
		return interestAmount;
	}
	
	public long getAmountPaid() {
		return amountPaid;
	}
	
	public long getAmountToPay() {
		return amountToPay;
	}
	
	public Date getPaidDT() {
		return paidDT;
	}
}