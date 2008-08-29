package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;


/**
 * 
 * A RecurringOffer extends the class  {@link Offer} is basically a collection of {@link Article}s along with
 * status information as its name implies the RecurringOffer is in a recurring state ,
 * defined by the timer Task in configuration class  {@link RecurringOfferConfiguration} 
 * 
 * 
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

	
	/**
	 * the Problem key Constants to store the state of the Offer
	 * see  {@link #ProblemKey} 
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_RECURRING_OFFER_CONFIGURATION = "RecurringOffer.recurringOfferConfiguration";

	public static final String PROBLEM_KEY_NONE = null;

	public static final String PROBLEM_KEY_PRICE_NONEQUAL = "Prices Non-Equal";

	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="recurringOffer"
	 */
	private RecurringOfferConfiguration recurringOfferConfiguration;

	/**
	 * the ProblemKey stores the error statues upon the validation of the RecurringOffer
	 * see the method  {@link  RecurringTrader.processRecurringOffer(RecurringOffer)}
	 * also see the constants  {@link #PROBLEM_KEY_NONE} {@link #PROBLEM_KEY_PRICE_NONEQUAL} 
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String problemKey;
	
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOffer() {}

	public RecurringOffer(User user, Order order, String offerIDPrefix, long offerID) {
		super(user, order, offerIDPrefix, offerID);
		this.recurringOfferConfiguration = new RecurringOfferConfiguration(
				this, user, getOrganisationID(), IDGenerator.nextID(RecurringOfferConfiguration.class));
		this.problemKey = PROBLEM_KEY_NONE;
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

	public String getProblemKey() {
		return problemKey;
	}

	public void setProblemKey(String problemKey) {
		this.problemKey = problemKey;
	}




}
