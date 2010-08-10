package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
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
import org.nightlabs.jfire.trade.recurring.RecurringTradeManagerLocal;
import org.nightlabs.jfire.trade.recurring.RecurringTrader;

/**
 * A DunningConfig contains all settings for a dunning process. 
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningConfigID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningConfig"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningConfig.class);

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
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_invoiceDunningStepss",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private SortedSet<InvoiceDunningStep> invoiceDunningSteps;
	
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
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_processDunningSteps",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private SortedSet<ProcessDunningStep> processDunningSteps;
	
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
			"This instance of Trader is currently not attached to a datastore! Cannot get a PersistenceManager!");

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
		
		this.name = new DunningConfigName(this);
		this.description = new DunningConfigDescription(this);
		
		this.invoiceDunningSteps = new TreeSet<InvoiceDunningStep>();
		this.processDunningSteps = new TreeSet<ProcessDunningStep>();
		
		this.dunningInterestCalculator = new DunningInterestCalculatorCustomerFriendly();
		this.dunningFeeAdder = new DunningFeeAdderCustomerFriendly(organisationID);
		
		TaskID taskID = TaskID.create(organisationID, TASK_TYPE_ID_PROCESS_DUNNING, dunningConfigID);
		this.creatorTask = new Task(
				taskID,
				User.getUser(getPersistenceManager(), getOrganisationID(), User.USER_ID_SYSTEM),
				DunningManagerRemote.class,
				"processAutomaticDunning"
		);
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
	
	public DunningAutoMode getDunningAutoMode() {
		return dunningAutoMode;
	}
	
	public long getDefaultTermOfPaymentMSec() {
		return defaultTermOfPaymentMSec;
	}
	
	public DunningInterestCalculator getDunningInterestCalculator() {
		return dunningInterestCalculator;
	}
	
	public SortedSet<InvoiceDunningStep> getInvoiceDunningSteps() {
		return invoiceDunningSteps;
	}
	
	public SortedSet<ProcessDunningStep> getProcessDunningSteps() {
		return processDunningSteps;
	}
	
	public DunningConfigName getName() {
		return name;
	}
}