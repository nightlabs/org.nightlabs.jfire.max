package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;


/**
 *
 * @author Fitas Amine <fitas@nightlabs.de>
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Order"
 *		detachable="true"
 *		table="JFireTrade_Recurring"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 *
 *
 **/
public class RecurringOrder extends Order {

	public RecurringOrder(LegalEntity vendor, LegalEntity customer,
			String orderIDPrefix, long orderID, Currency currency, User user) {
		super(vendor, customer, orderIDPrefix, orderID, currency, user);
		// TODO Auto-generated constructor stub
	}

}
