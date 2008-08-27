package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;


/**
 *
 * A {@link RecurringOrder} is an extention of {@link Order} thus it is a collection of {@link Offer}s between two {@link LegalEntity}s,
 * it knows all {@link Article} of the contained {@link Offer}.
 * 
 * @author Fitas Amine <fitas@nightlabs.de>
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Order"
 *		detachable="true"
 *		table="JFireTrade_RecurringOrder"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 **/
public class RecurringOrder extends Order {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOrder() { }
	
	public RecurringOrder(LegalEntity vendor, LegalEntity customer,
			String orderIDPrefix, long orderID, Currency currency, User user) {
		super(vendor, customer, orderIDPrefix, orderID, currency, user);
	}

}