package org.nightlabs.jfire.dunning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
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
 * There are costs that may only be added once per DunningLetter. 
 * The ProcessDunningStep is used to apply these and to know which 
 * layout to use for a DunningLetter of a particular dunning level.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ProcessDunningStep"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ProcessDunningStep.FETCH_GROUP_DUNNING_FEE_TYPE,
		members=@Persistent(name="feeTypes")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=ProcessDunningStep.FETCH_GROUP_LETTER_LAYOUT,
		members=@Persistent(name="letterLayout")
	)
})
public class ProcessDunningStep 
extends AbstractDunningStep
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProcessDunningStep.class);
	
	public static final String FETCH_GROUP_DUNNING_FEE_TYPE = "ProcessDunningStep.feeTypes";
	public static final String FETCH_GROUP_LETTER_LAYOUT = "ProcessDunningStep.letterLayout";
	/**
	 * The fees that should be applied to the new DunningLetter. 
	 * 
	 * Note, that all fees from the previous DunningLetter need to be 
	 * copied into the new DunningLetter, additionally.
	 */
	@Join
	@Persistent(
		table="JFireDunning_ProcessDunningStep_feeTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningFeeType> feeTypes;
	
	/**
	 * The description of how the DunningLetter should look like.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ReportLayout letterLayout;
	
	/**
	 * The coolDownPeriod is the timespan from the last DunningLetter, 
	 * in which no new DunningLetters are to be created.
	 * 
	 * Therefore, manual triggering of a DunningProcess check does not 
	 * flood the customer with DunningLetters.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long coolDownPeriod;
	
	public ProcessDunningStep(String organisationID, String dunningStepID, DunningConfig dunningConfig, int dunningLevel) {
		super(organisationID, dunningStepID, dunningConfig, dunningLevel);
		
		this.feeTypes = new ArrayList<DunningFeeType>();
	}
	
	public void addFeeType(DunningFeeType feeType) {
		feeTypes.add(feeType);
	}
	
	public void addFeeTypes(Collection<DunningFeeType> feeTypes) {
		feeTypes.addAll(feeTypes);
	}
	
	public void removeFeeType(DunningFeeType feeType) {
		feeTypes.remove(feeType);
	}
	
	public void removeFeeTypes(Collection<DunningFeeType> feeTypes) {
		feeTypes.removeAll(feeTypes);
	}

	public List<DunningFeeType> getFeeTypes() {
		return Collections.unmodifiableList(feeTypes);
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
