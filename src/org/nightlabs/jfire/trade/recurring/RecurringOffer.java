package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;


/**
 * @author Fitas Amine <fitas@nightlabs.de>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Offer"
 *		detachable="true"
 *		table="JFireTrade_RecurringOffer"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 * 
 * @jdo.fetch-group name="RecurringOffer.recurringOfferConfiguration" fields="recurringOfferConfiguration"
 * 
 */
public class RecurringOffer extends Offer {

	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_RECURRING_OFFER_CONFIGURATION = "RecurringOffer.recurringOfferConfiguration";

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="recurringOffer"
	 */
	private RecurringOfferConfiguration recurringOfferConfiguration;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOffer() {}

	public RecurringOffer(User user, Order order, String offerIDPrefix, long offerID) {
		super(user, order, offerIDPrefix, offerID);
		this.recurringOfferConfiguration = new RecurringOfferConfiguration(
				this, user, getOrganisationID(), IDGenerator.nextID(RecurringOfferConfiguration.class));
	}

	@Override
	protected boolean validate() {
		boolean superResult = super.validate();
		if (superResult) {
			if (getRecurringOfferConfiguration().getCreatorTask().getParam() == null) {
				getRecurringOfferConfiguration().getCreatorTask().setParam(this);
			}
		}
		return superResult;
	}
	
	public RecurringOfferConfiguration getRecurringOfferConfiguration() {
		return recurringOfferConfiguration;
	}




}
