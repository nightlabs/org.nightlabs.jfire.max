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
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private BigDecimal interestPercentage;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long periodOfGraceMSec;
	
	public InvoiceDunningStep(String organisationID, String dunningStepID, DunningConfig dunningConfig, int dunningLevel) {
		super(organisationID, dunningStepID, dunningConfig, dunningLevel);
	}
	
	public BigDecimal getInterestPercentage() {
		return interestPercentage;
	}
	
	public long getPeriodOfGraceMSec() {
		return periodOfGraceMSec;
	}
}
