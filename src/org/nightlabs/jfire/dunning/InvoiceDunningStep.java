package org.nightlabs.jfire.dunning;

import java.math.BigDecimal;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;

/**
 * An InvoiceDunningStep is used when calculating the additional interest cost 
 * of an overdue invoice. It specifies all the information that is needed to 
 * calculate the interest fees for a particular dunning level and knows the time 
 * interval according to which the due date is shifted.
 * 
 * Note that one DunningProcess may cope with several overdue invoices of one 
 * customer that are combined in one DunningLetter. Furthermore there are fees 
 * that are applied only to one DunningLetter and not for each affected invoice. 
 * Therefore the InvoiceDunningStep is complemented by the ProcessDunningStep 
 * which is used to compute the fees of one DunningLetter and which layout to 
 * use for that letter.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_InvoiceDunningStep"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class InvoiceDunningStep 
extends AbstractDunningStep 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(InvoiceDunningStep.class);
	
	/**
	 * The percentage that should be applied when calculating the interest.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private BigDecimal interestPercentage;
	
	/**
	 * The time (in milliseconds) after this DunningStep, before continuing 
	 * the DunningProcess (i.e. how long to wait before performing further action).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long periodOfGraceMSec;
	
	public InvoiceDunningStep(String organisationID, String dunningStepID, DunningConfig dunningConfig, int dunningLevel) {
		super(organisationID, dunningStepID, dunningConfig, dunningLevel);
	}
	
	public void setInterestPercentage(BigDecimal interestPercentage) {
		this.interestPercentage = interestPercentage;
	}
	
	public BigDecimal getInterestPercentage() {
		return interestPercentage;
	}
	
	public void setPeriodOfGraceMSec(long periodOfGraceMSec) {
		this.periodOfGraceMSec = periodOfGraceMSec;
	}
	
	public long getPeriodOfGraceMSec() {
		return periodOfGraceMSec;
	}
}
