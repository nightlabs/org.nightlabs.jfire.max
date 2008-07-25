package org.nightlabs.jfire.trade.recurring;

import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;


/**
 * @author Fitas Amine <fitas@nightlabs.de>
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Offer"
 *		detachable="true"
 *		table="JFireTrade_RecurringOffer"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="RecurringOffer.recurringOfferConfiguration" fields="recurringOfferConfiguration"
 * 
 * 
 * 
 */
public class RecurringOffer extends Offer {


	public static final String FETCH_GROUP_RECURRING_OFFER_CONFIGURATION = "RecurringOffer.recurringOfferConfiguration";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private RecurringOfferConfiguration recurringOfferConfiguration;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOffer() {}

	public RecurringOffer(User user, Order order, String offerIDPrefix,
			long offerID) {
		super(user, order, offerIDPrefix, offerID);

		recurringOfferConfiguration = null;

	}

	public RecurringOfferConfiguration getRecurringOfferConfiguration() {
		return recurringOfferConfiguration;
	}




}
