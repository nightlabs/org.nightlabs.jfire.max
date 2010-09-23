package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.TimePatternFormatException;

/**
 * A DunningConfig contains all settings for a dunning process. 
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningConfigID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningConfig")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfig.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfig.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningConfig.FETCH_GROUP_DUNNING_AUTO_MODE,
		members=@Persistent(name="dunningAutoMode")
	),
	@FetchGroup(
		name=DunningConfig.FETCH_GROUP_PROCESS_DUNNING_STEPS,
		members=@Persistent(name="processDunningSteps")
	),
	@FetchGroup(
		name=DunningConfig.FETCH_GROUP_INVOICE_DUNNING_STEPS,
		members=@Persistent(name="invoiceDunningSteps")
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningConfig.class);

	public static final String FETCH_GROUP_NAME = "DunningConfig.name";
	public static final String FETCH_GROUP_DESCRIPTION = "DunningConfig.description";
	public static final String FETCH_GROUP_DUNNING_AUTO_MODE = "DunningConfig.dunningAutoMode";
	public static final String FETCH_GROUP_PROCESS_DUNNING_STEPS = "DunningConfig.processDunningSteps";
	public static final String FETCH_GROUP_INVOICE_DUNNING_STEPS = "DunningConfig.invoiceDunningSteps";
	
	public static final String TASK_TYPE_ID_PROCESS_DUNNING = "DunningTask";
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningConfigID;
	
	/**
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningConfig"
	)
	private DunningConfigName name;
	
	/**
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningConfig"
	)
	private DunningConfigDescription description;
	
	/**
	 * The time (in milliseconds) within which the payment is due. 
	 * It is copied to the Invoice, when a new invoice is created. 
	 * We might need new a hook in JFire to be able to react on the 
	 * invoice creation – or maybe we can simply use some default 
	 * JDO lifecycle listener.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long defaultTermOfPaymentMSec;
	
	/**
	 * Whether and what to do automatically.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningAutoMode dunningAutoMode;
	
	/**
	 * The steps that need to be performed for one particular 
	 * invoice of a dunning process. The level (or severeness) 
	 * specifies the ordering of the dunning steps.
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningConfig_invoiceDunningSteps",
		mappedBy="dunningConfig",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<InvoiceDunningStep> invoiceDunningSteps;
	
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient SortedSet<InvoiceDunningStep> sortedInvoiceDunningSteps;
	/**
	 * According to the dunning level of a process (most likely 
	 * the highest level of any invoice contained) different 
	 * fees have to be included, different layouts have to be 
	 * chosen and particular listener may have to be notified. 
	 * 
	 * This information is encapsulated in the ProcessDunningStep, 
	 * which are ordered according to the dunning level of the dunning step.
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningConfig_processDunningSteps",
		mappedBy="dunningConfig",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<ProcessDunningStep> processDunningSteps;
	
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient SortedSet<ProcessDunningStep> sortedProcessDunningSteps;
	
	/**
	 * The calculator implementation used for the calculation of 
	 * interests – there are many different ways to calculate them.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningInterestCalculator dunningInterestCalculator;
	
	/**
	 * Encapsulates the logic for deciding what fees to add to 
	 * a newly created DunningLetter.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningFeeAdder dunningFeeAdder;
	
	/**
	 * Maps from a dunning level to the notifers that want to be triggered 
	 * if a letter with the corresponding dunning level is created.
	 */
	@Join
	@Persistent(table="JFireDunning_DunningConfig_level2DunningLetterNotifiers")
	private Map<Integer, DunningLetterNotifier> level2DunningLetterNotifiers;
	
	/**
	 * The configuration of the accounts  the fees and interests are booked to.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningMoneyFlowConfig moneyFlowConfig;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Task creatorTask;
	
	/**
	 * @return the {@link PersistenceManager} associated with this {@link DunningConfig}
	 * will fail if the instance is not attached.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of DunningConfig is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}
	
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningConfig() { }
	
	/**
	 * Create an instance of <code>DunningConfig</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningConfigID second part of the primary key. A local identifier within the namespace of the organisation.
	 * @see #DunningConfig(boolean)
	 */
	public DunningConfig(String organisationID, String dunningConfigID, DunningAutoMode dunningAutoMode) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningConfigID, "dunningConfigID"); //$NON-NLS-1$
		
		this.organisationID = organisationID;
		this.dunningConfigID = dunningConfigID;
		this.dunningAutoMode = dunningAutoMode;
		
		this.defaultTermOfPaymentMSec = TimeUnit.DAYS.toMillis(31);
		
		this.name = new DunningConfigName(this);
		this.description = new DunningConfigDescription(this);
		
		this.invoiceDunningSteps = new TreeSet<InvoiceDunningStep>();
		this.processDunningSteps = new TreeSet<ProcessDunningStep>();
		
		TaskID taskID = TaskID.create(organisationID, TASK_TYPE_ID_PROCESS_DUNNING, dunningConfigID);
		this.creatorTask = new Task(
				taskID,
				User.getUser(getPersistenceManager(), getOrganisationID(), User.USER_ID_SYSTEM),
				DunningManagerRemote.class,
				"processAutomaticDunning"
		);
		this.creatorTask.setParam(this);
		try {
			creatorTask.getTimePatternSet().createTimePattern(
					"*", // year
					"*", // month
					"31", // day
					"*", // dayOfWeek
					"*", //  hour
			"*"); // minute
		} catch (TimePatternFormatException e) {
			throw new RuntimeException(e);
		}

		creatorTask.setEnabled(true);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningConfigID() {
		return dunningConfigID;
	}

	public DunningConfigDescription getDescription() {
		return description;
	}
	
	public void setDunningAutoMode(DunningAutoMode dunningAutoMode) {
		this.dunningAutoMode = dunningAutoMode;
	}
	
	public DunningAutoMode getDunningAutoMode() {
		return dunningAutoMode;
	}
	
	public void setDefaultTermOfPaymentMSec(long defaultTermOfPaymentMSec) {
		this.defaultTermOfPaymentMSec = defaultTermOfPaymentMSec;
	}
	
	public void setDefaultTermOfPaymentDay(int defaultTermOfPaymentDay) {
		this.defaultTermOfPaymentMSec = defaultTermOfPaymentDay * (24l * 60l * 60l * 1000l);
	}
	
	public long getDefaultTermOfPaymentMSec() {
		return defaultTermOfPaymentMSec;
	}
	
	public int getDefaultTermOfPaymentDay() {
		return (int)(defaultTermOfPaymentMSec / (24l * 60l * 60l * 1000l));
	}
	
	public void setDunningInterestCalculator(
			DunningInterestCalculator dunningInterestCalculator) {
		this.dunningInterestCalculator = dunningInterestCalculator;
	}
	
	public DunningInterestCalculator getDunningInterestCalculator() {
		return dunningInterestCalculator;
	}
	
	public void setDunningFeeAdder(DunningFeeAdder dunningFeeAdder) {
		this.dunningFeeAdder = dunningFeeAdder;
	}
	
	public DunningFeeAdder getDunningFeeAdder() {
		return dunningFeeAdder;
	}
	
	public boolean addInvoiceDunningStep(InvoiceDunningStep invoiceDunningStep) {
		getInvoiceDunningSteps().add(invoiceDunningStep);
		invoiceDunningSteps.clear();
		return invoiceDunningSteps.addAll(sortedInvoiceDunningSteps);
	}
	
	public boolean removeInvoiceDunningStep(InvoiceDunningStep invoiceDunningStep) {
		getInvoiceDunningSteps().remove(invoiceDunningStep);
		invoiceDunningSteps.clear();
		return invoiceDunningSteps.addAll(sortedInvoiceDunningSteps);
	}
	
	public SortedSet<InvoiceDunningStep> getInvoiceDunningSteps() {
		if (sortedInvoiceDunningSteps == null) {
			sortedInvoiceDunningSteps = new TreeSet<InvoiceDunningStep>();
			for (InvoiceDunningStep is : invoiceDunningSteps) {
				sortedInvoiceDunningSteps.add(is);
			}
		}
		return sortedInvoiceDunningSteps;
	}
	
	public boolean addProcessDunningStep(ProcessDunningStep processDunningStep) {
		getProcessDunningSteps().add(processDunningStep);
		processDunningSteps.clear();
		return processDunningSteps.addAll(sortedProcessDunningSteps);
	}
	
	public boolean removeProcessDunningStep(ProcessDunningStep processDunningStep) {
		getProcessDunningSteps().remove(processDunningStep);
		processDunningSteps.clear();
		return processDunningSteps.addAll(sortedProcessDunningSteps);
	}
	
	public SortedSet<ProcessDunningStep> getProcessDunningSteps() {
		if (sortedProcessDunningSteps == null) {
			sortedProcessDunningSteps = new TreeSet<ProcessDunningStep>();
			for (ProcessDunningStep is : processDunningSteps) {
				sortedProcessDunningSteps.add(is);
			}
		}
		return sortedProcessDunningSteps;
	}
	
	public ProcessDunningStep getProcessDunningStep(int level) {
		for (ProcessDunningStep processDunningStep : processDunningSteps) {
			if (processDunningStep.getDunningLevel() == level) {
				return processDunningStep;
			}
		}
		return null;
	}

	public InvoiceDunningStep getInvoiceDunningStep(int level) {
		for (InvoiceDunningStep invoiceDunningStep : invoiceDunningSteps) {
			if (invoiceDunningStep.getDunningLevel() == level) {
				return invoiceDunningStep;
			}
		}
		return null;
	}
	
	public Map<Integer, DunningLetterNotifier> getLevel2DunningLetterNotifiers() {
		return level2DunningLetterNotifiers;
	}
	
	public void setMoneyFlowConfig(DunningMoneyFlowConfig moneyFlowConfig) {
		this.moneyFlowConfig = moneyFlowConfig;
	}
	
	public DunningMoneyFlowConfig getMoneyFlowConfig() {
		return moneyFlowConfig;
	}
	
	public DunningConfigName getName() {
		return name;
	}
	
	public Task getCreatorTask() {
		return creatorTask;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dunningConfigID == null) ? 0 : dunningConfigID.hashCode());
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
		DunningConfig other = (DunningConfig) obj;
		if (dunningConfigID == null) {
			if (other.dunningConfigID != null)
				return false;
		} else if (!dunningConfigID.equals(other.dunningConfigID))
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
		return "DunningConfig [dunningConfigID=" + dunningConfigID
				+ ", organisationID=" + organisationID + "]";
	}
}