package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.timer.Task;


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

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long recurringOfferConfigurationID;

	public RecurringOfferConfiguration(String organisationID,
			long recurringOfferConfigurationID) {
		this.organisationID = organisationID;
		this.recurringOfferConfigurationID = recurringOfferConfigurationID;
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
}
