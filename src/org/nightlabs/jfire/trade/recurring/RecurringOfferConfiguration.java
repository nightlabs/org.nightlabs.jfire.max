package org.nightlabs.jfire.trade.recurring;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;


/**
 * An instance of {@link RecurringOfferConfiguration} is created for each {@link RecurringOffer}. 
 * It stores the user-definable configuration for the timed processing of a {@link RecurringOffer}.
 * This information contains whether an invoice should be automatically created for the
 * {@link RecurredOffer}, whether this invoice should be booked and more.
 * <p>
 * Additionally {@link RecurringOfferConfiguration} creates and holds the {@link Task} that will
 * do the processing of the {@link RecurringOffer} 
 * (the task will execute {@link RecurringTradeManager#processRecurringOfferTimed(TaskID)} which 
 * itself will delegate to {@link RecurringTrader#processRecurringOffer(RecurringOffer)})
 * </p>
 * 
 * @author Fitas Amine <fitas@nightlabs.de>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.recurring.id.RecurringOfferConfigurationID"
 *		detachable="true"
 *		table="JFireTrade_RecurringOfferConfiguration"
 *
 * @jdo.version strategy="version-number"
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, recurringOfferConfigurationID"
 *
 * @jdo.fetch-group name="RecurringOfferConfiguration.creatorTask" fields="creatorTask"
 */
public class RecurringOfferConfiguration implements Serializable{

	private static final long serialVersionUID = 20080818L;
	public static final String TASK_TYPE_ID_RECURRED_OFFER_CREATOR_TASK = "RecurredOfferCreatorTask";

	public static final String FETCH_GROUP_CREATOR_TASK = "RecurringOfferConfiguration.creatorTask"; 

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long recurringOfferConfigurationID;
	
	/**
	 * Create a new {@link RecurringOfferConfiguration}. 
	 * 
	 * @param recurringOffer The {@link RecurringOffer} the new configuration is for.
	 * @param user The user that initiated the creation.
	 * @param organisationID The organisationID of the new {@link RecurringOfferConfiguration}.
	 * @param recurringOfferConfigurationID The id of the new {@link RecurringOfferConfiguration}.
	 */
	public RecurringOfferConfiguration(RecurringOffer recurringOffer,User user,String organisationID,long recurringOfferConfigurationID) {
		this.organisationID = organisationID;
		this.recurringOffer = recurringOffer;
		this.recurringOfferConfigurationID = recurringOfferConfigurationID;
		String _taskID =  ObjectIDUtil.longObjectIDFieldToString(recurringOfferConfigurationID);
		TaskID taskID = TaskID.create(organisationID,TASK_TYPE_ID_RECURRED_OFFER_CREATOR_TASK, _taskID);
		this.creatorTask = new Task(
				taskID,
				user,
				RecurringTradeManagerHome.JNDI_NAME,
		"processRecurringOfferTimed");	
		suspendDate = null;
		
		
		
		
		// We can not set the parameter here in the constructor as Task.setParam will call makePersistent() 
		// for the given parameter if it's not, and so it will try to persist the object we are currently creating
		// This is now handled by RecurringOffer.validate()
//		creatorTask.setParam(recurringOffer);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOfferConfiguration() {}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean createInvoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date suspendDate;
	
	
	
	/**
	 * Whether we book the invoice implicitly
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean bookInvoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private RecurringOffer recurringOffer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * 
	 * create a delivery note
	 */
	private boolean createDelivery;

	/**
	 * the Task timer used to define at what moment we call the EJB method 
	 * to process the {@link RecurringOffer} , see {@link RecurringTrade.processRecurringOffer(RecurringOffer)} 
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Task creatorTask;

	/**
	 * Returns whether upon processing of the {@link RecurringOffer} 
	 * an {@link Invoice} should be created for the articles of the 
	 * {@link RecurredOffer}.
	 *  
	 * @return Whether an invoice should be automatically created.
	 */
	public boolean isCreateInvoice() {
		return createInvoice;
	}

	/**
	 * Define whether upon processing of the {@link RecurringOffer} 
	 * an {@link Invoice} should be created for the articles of the 
	 * {@link RecurredOffer}.
	 * 
	 * @param createInvoice Whether an invoice should be created automatically.
	 */
	public void setCreateInvoice(boolean createInvoice) {
		this.createInvoice = createInvoice;
	}

	/**
	 * Returns whether upon processing of the {@link RecurringOffer} 
	 * an delivery should be made for the articles of the 
	 * {@link RecurredOffer}.
	 * 
	 * @return Whether a delivery should be made.
	 */
	public boolean isCreateDelivery() {
		return createDelivery;
	}

	/**
	 * Define whether upon processing of the {@link RecurringOffer} 
	 * an delivery should be made for the articles of the 
	 * {@link RecurredOffer.
	 * 
	 * @param createDelivery Whether a delivery should be made.
	 */
	public void setCreateDelivery(boolean createDelivery) {
		this.createDelivery = createDelivery;
	}

	/**
	 * Get the {@link Task} that was created for this {@link RecurringOfferConfiguration}
	 * processes the associated {@link RecurringOffer}. 
	 * 
	 * @return The {@link Task} that processes the associated {@link RecurringOffer}.
	 */
	public Task getCreatorTask() {
		return creatorTask;
	}

	/**
	 * @return The organisationID pk-member of this {@link RecurringOfferConfiguration}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return The recurringOfferConfigurationID pk-member of this {@link RecurringOfferConfiguration}.
	 */
	public long getRecurringOfferConfigurationID() {
		return recurringOfferConfigurationID;
	}
	
	/**
	 * Returns the {@link RecurringOffer} this configuration is associated to.
	 * @return The {@link RecurringOffer} this configuration is associated to.
	 */
	public RecurringOffer getRecurringOffer() {
		return recurringOffer;
	}

	/**
	 * Returns the {@link TaskID} of the {@link Task} that processes 
	 * the associated {@link RecurringOffer}.
	 * 
	 * @return The {@link TaskID} for the {@link Task} that processes
	 *         the associated {@link RecurringOffer}. 
	 */
	public TaskID getCreatorTaskID() {
		TaskID taskID = (TaskID) JDOHelper.getObjectId(creatorTask);
		if (taskID == null && creatorTask != null)
			throw new IllegalStateException("creatorTask has not yet been persisted!"); // if you really come into this situation, either create the TaskID manually by TaskID.create(...) or persist first (and maybe detach) before accessing this method.

		return taskID;
	}

	/**
	 * Returns whether upon processing of the {@link RecurringOffer} 
	 * if an invoice was created for the new {@link RecurredOffer}
	 * this invoice should be booked/finalized, too.
	 * 
	 * @return Whether the invoice created upon processing should be booked, too.
	 */
	public boolean isBookInvoice() {
		return bookInvoice;
	}

	/**
	 * Define whether upon processing of the {@link RecurringOffer} 
	 * if an invoice was created for the new {@link RecurredOffer}
	 * this invoice should be booked/finalized, too.
	 * 
	 * @param isBookInvoice Whether the invoice created upon processing should be booked, too.
	 */
	public void setBookInvoice(boolean isBookInvoice) {
		this.bookInvoice = isBookInvoice;
	}

	public Date getSuspendDate() {
		return suspendDate;
	}

	public void setSuspendDate(Date suspendDate) {
		this.suspendDate = suspendDate;
	}






}
