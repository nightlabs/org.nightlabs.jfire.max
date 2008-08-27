package org.nightlabs.jfire.trade.recurring;

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;


/**
 * 
 * An instance of <tt>RecurringOfferConfiguration</tt> is created on a new {@link RecurringOffer}. 
 * it stores the various config information about how we should create and manipulate the {@link RecurringOffer}
 * as we could define settings such as booking Invoice , Delivery etc..
 * the classs initite and New Timer Task class {@link Task}. 
 * 
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
	private boolean bookInvoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private RecurringOffer recurringOffer;


	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean createDelivery;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Task creatorTask;

	public boolean isCreateInvoice() {
		return createInvoice;
	}

	public void setCreateInvoice(boolean createInvoice) {
		this.createInvoice = createInvoice;
	}

	public boolean isCreateDelivery() {
		return createDelivery;
	}

	public void setCreateDelivery(boolean createDelivery) {
		this.createDelivery = createDelivery;
	}

	public Task getCreatorTask() {
		return creatorTask;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getRecurringOfferConfigurationID() {
		return recurringOfferConfigurationID;
	}

	public RecurringOffer getRecurringOffer() {
		return recurringOffer;
	}

	public TaskID getCreatorTaskID() {
		TaskID taskID = (TaskID) JDOHelper.getObjectId(creatorTask);
		if (taskID == null && creatorTask != null)
			throw new IllegalStateException("creatorTask has not yet been persisted!"); // if you really come into this situation, either create the TaskID manually by TaskID.create(...) or persist first (and maybe detach) before accessing this method.

		return taskID;
	}

	public boolean isBookInvoice() {
		return bookInvoice;
	}

	public void setBookInvoice(boolean isBookInvoice) {
		this.bookInvoice = isBookInvoice;
	}
}
