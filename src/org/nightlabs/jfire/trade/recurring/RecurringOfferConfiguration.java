package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;


/**
 * @author Fitas Amine <fitas@nightlabs.de>
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
 */
public class RecurringOfferConfiguration {

	private static final long serialVersionUID = 1L;
	public static final String TASK_TYPE_ID_RECURRED_OFFER_CREATOR_TASK = "RecurredOfferCreatorTask";
	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long recurringOfferConfigurationID;

	public RecurringOfferConfiguration(User user,String organisationID,
			long recurringOfferConfigurationID) {
		this.organisationID = organisationID;
		this.recurringOfferConfigurationID = recurringOfferConfigurationID;
	    String _taskID =  ObjectIDUtil.longObjectIDFieldToString(recurringOfferConfigurationID);
		TaskID taskID = TaskID.create(organisationID,TASK_TYPE_ID_RECURRED_OFFER_CREATOR_TASK, _taskID);
		this.creatorTask = new Task(
	    	    taskID,
	    	    user,
	    	    "",
	    	    "");	
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
	private boolean createDelivery;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Task creatorTask;

	public boolean isCreateInvoice() {
		return createInvoice;
	}

	public boolean isCreateDelivery() {
		return createDelivery;
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
}
