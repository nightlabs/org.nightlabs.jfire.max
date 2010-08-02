package org.nightlabs.jfire.dunning;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.reporting.layout.ReportLayout;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ProcessDunningStep"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProcessDunningStep 
extends AbstractDunningStep 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProcessDunningStep.class);
	
	@Join
	@Persistent(
		table="JFireDunning_ProcessDunningStep_feeTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningFeeType> feeTypes;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ReportLayout letterLayout;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long coolDownPeriod;
	
	public ProcessDunningStep(String organisationID, String dunningStepID, DunningConfig dunningConfig, int dunningLevel) {
		super(organisationID, dunningStepID, dunningConfig, dunningLevel);
		
		this.feeTypes = new ArrayList<DunningFeeType>();
	}
	
	public List<DunningFeeType> getFeeTypes() {
		return feeTypes;
	}
	
	public void setLetterLayout(ReportLayout letterLayout) {
		this.letterLayout = letterLayout;
	}
	
	public ReportLayout getLetterLayout() {
		return letterLayout;
	}
	
	public void setCoolDownPeriod(long coolDownPeriod) {
		this.coolDownPeriod = coolDownPeriod;
	}
	
	public long getCoolDownPeriod() {
		return coolDownPeriod;
	}
}
