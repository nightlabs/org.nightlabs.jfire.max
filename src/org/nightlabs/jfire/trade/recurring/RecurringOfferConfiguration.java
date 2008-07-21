package org.nightlabs.jfire.trade.recurring;


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
 * @jdo.create-objectid-class field-order="organisationID, RecurringOfferConfigurationID"
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
	private long RecurringOfferConfigurationID;
	
	public RecurringOfferConfiguration(String organisationID,
			long recurringOfferConfigurationID) {
		this.organisationID = organisationID;
		RecurringOfferConfigurationID = recurringOfferConfigurationID;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOfferConfiguration() {}

	boolean createInvoice()
	{
		return false;
	}

	boolean createDelivery()
	{
		return false;
	}

}
