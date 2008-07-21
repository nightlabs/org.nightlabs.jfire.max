package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.trade.Order;
/**
 *
 * @author Fitas Amine <fitas@nightlabs.de>

 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.id.RecurringOrderID"
 *		detachable="true"
 *		table="JFireTrade_Recurring"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 *
 **/

public class RecurringOrder extends Order {

}
