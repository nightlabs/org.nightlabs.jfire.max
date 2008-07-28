package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;


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
 */
public class RecurredOffer extends Offer {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private RecurringOffer recurringOffer;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurredOffer() {}

	public RecurredOffer(User user, Order order, String offerIDPrefix,
			long offerID) {
		super(user, order, offerIDPrefix, offerID);

	}

	/**
	 * Returns the {@link RecurringOffer} that was the template 
	 * for this {@link RecurredOffer}.
	 *   
	 * @return the {@link RecurringOffer} that was the template for this {@link RecurredOffer}.
	 */
	public RecurringOffer getRecurringOffer() {
		return recurringOffer;
	}

}
