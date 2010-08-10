package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;

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
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.organisation.Organisation;

import com.mckoi.util.BigNumber;

/**
 * According to http://zinsmethoden.de/ (unfortunately only in German), 
 * there are many possibilities to calculate an interest. The questions 
 * that need to be decided by this calculator are: <br>
 
 * <br>1.How many days does the year have? This is important to divide the percentage accordingly (i.e. is a delay of payment of 1 day calculated as dueAmount * percentage / 360 or dueAmount * percentage / 365 or is the real calendar used)?
 * <br>2.What's the first day in the interest calculation? Is it the due date or the following day at midnight?
 * <br>3.What's the last day in the interest calculation? There are many options: The new due date, the finalization date of the DunningLetter or the creation date of the DunningLetter. And is this day included or excluded in the period?
 * 
 * <br><br>Due to all these questions, the DunningInterestCalculator is abstract. But we provide a pretty customer-friendly default implementation (see below).

 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningInterestCalculatorID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningInterestCalculator"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningInterestCalculator 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningInterestCalculator.class);
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningInterestCalculator() { }
	
	public abstract int getDays();
	
	public abstract Date getFirstDay();
	
	public abstract Date getLastDay();
	
	public BigNumber calculateInterest() {
//		dunningConfig.getInvoiceDunningSteps().
		return null;
	}
	
	/**
	 * Create an instance of <code>DunningInterestCalculator</code>.
	 *
	 */
	public DunningInterestCalculator(String organisationID, DunningConfig dunningConfig) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningConfig = dunningConfig;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}
}