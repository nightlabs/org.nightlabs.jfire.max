package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;





public class RecurringOffer extends Offer {

	private RecurringOfferConfiguration recurringOfferConfiguration;
	
	
	public RecurringOffer(User user, Order order, String offerIDPrefix,
			long offerID) {
		super(user, order, offerIDPrefix, offerID);
		
		recurringOfferConfiguration = null;
		
	}


	
}
