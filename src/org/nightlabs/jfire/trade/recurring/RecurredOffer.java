package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.trade.Offer;
/**
 * @author Fitas Amine <fitas@nightlabs.de>
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Offer"
 *		detachable="true"
 *		table="JFireTrade_RecurredOffer"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="RecurringOffer.recurringOffer" fields="recurringOffer"
 * 
 * 
 * 
 */
public class RecurredOffer extends Offer{

	private RecurringOffer recurringOffer;


}
