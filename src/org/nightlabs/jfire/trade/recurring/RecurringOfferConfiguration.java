package org.nightlabs.jfire.trade.recurring;


/*
 * @author Fitas Amine <fitas@nightlabs.de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.RecurringOfferConfigurationID"
 *		detachable="true"
 *		table="JFireTrade_RecurringOfferConfiguration"
 *
 * @jdo.version strategy="version-number"
 * @jdo.inheritance strategy="new-table"
 *
 *
 *
 */
public class RecurringOfferConfiguration {



	boolean createInvoice()
	{
		return false;
	}

	boolean createDelivery()
	{
		return false;
	}

}
