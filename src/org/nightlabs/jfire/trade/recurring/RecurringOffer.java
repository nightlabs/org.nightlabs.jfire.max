package org.nightlabs.jfire.trade.recurring;

import java.util.Collection;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;


/**
 * A {@link RecurringOffer} is an offer that serves as a template for the recurring/timed
 * creation of {@link RecurredOffer}s. All {@link Article}s of a {@link RecurringOffer} 
 * are reference a {@link ProductType} rather than a {@link Product}. When a {@link RecurringOffer}
 * is processed in the background an a new {@link RecurredOffer} is created for it
 * {@link Product} instances for the {@link ProductType}s referenced by the articles of 
 * the template {@link RecurringOffer} - this is done using {@link RecurringTradeProductTypeActionHandler}s. 
 * <p>
 * The configuration of a {@link RecurringOffer} is stored in its member {@link #getRecurringOfferConfiguration()}.
 * This {@link RecurringOfferConfiguration} defines the frequency new {@link RecurredOffer}s
 * should be created and has other controls that enable the user to define whether invoices should
 * be automatically created etc.. 
 * </p>
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

	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_RECURRING_OFFER_CONFIGURATION = "RecurringOffer.recurringOfferConfiguration";

	/**
	 * Constant indicating that no problem occurred on the last execution. Value is <code>null</code>.
	 */
	public static final String STATUS_KEY_NONE = "None";
	/**
	 * Constant indication that on the last processing a price difference was found between
	 * the articles in the newly create {@link RecurredOffer} and the {@link RecurringOffer}.
	 */
	public static final String STATUS_KEY_PRICES_NOT_EQUAL = "PricesNotEqual";

	
	/**
	 * the recurringOffer has been suspended due to stop date mostly
	 */
	public static final String STATUS_KEY_SUSPENDED = "Suspended";
	
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="recurringOffer"
	 */
	private RecurringOfferConfiguration recurringOfferConfiguration;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String statusKey;
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int recurredOfferCount;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOffer() {}

	/**
	 * Create a new {@link RecurringOffer}.
	 * @param user The user that initiated the creation.
	 * @param order The {@link RecurringOrder} the new {@link RecurringOffer} should be part of. 
	 * @param offerIDPrefix The offerID prefix to use.
	 * @param offerID The id for the new {@link RecurringOffer}.
	 */
	public RecurringOffer(User user, RecurringOrder order, String offerIDPrefix, long offerID) {
		super(user, order, offerIDPrefix, offerID);
		this.recurringOfferConfiguration = new RecurringOfferConfiguration(
				this, user, getOrganisationID(), IDGenerator.nextID(RecurringOfferConfiguration.class));
		this.statusKey = STATUS_KEY_NONE;
		this.recurredOfferCount = 0;
		
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Additionally to the super implementation {@link RecurringOffer}s
	 * set the param of their {@link RecurringOfferConfiguration}s task.
	 * </p>
	 */
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

	/**
	 * Get the {@link RecurringOfferConfiguration} associated to this {@link RecurringOffer}.
	 * @return The {@link RecurringOfferConfiguration} associated to this {@link RecurringOffer}.
	 */
	public RecurringOfferConfiguration getRecurringOfferConfiguration() {
		return recurringOfferConfiguration;
	}

	/**
	 * The ProblemKey stores the error statues upon the last processing
	 * and the attempt to create a {@link RecurredOffer} on its basis.
	 * See the method  {@link RecurringTrader#processRecurringOffer(RecurringOffer)}
	 * also see the constants  {@link #STATUS_KEY_NONE} {@link #PROBLEM_KEY_PRICE_NONEQUAL} 
	 * 
	 * @return The problem key set for the last processing.
	 */
	public String getStatusKey() {
		return statusKey;
	}

	/**
	 * Set the problem key.
	 * @param problemKey The problem key to set.
	 */
	public void setStatusKey(String statusKey) {
		this.statusKey = statusKey;
	}

	public int getRecurredOfferCount() {
		return recurredOfferCount;
	}

	protected void setRecurredOfferCount(int recurredOfferCount) {
		this.recurredOfferCount = recurredOfferCount;
	}
	
	@Override
	protected void checkArticles(Collection<? extends Article> articles) {
		for (Article article : articles) {
			if (article.getProduct() != null) {
				throw new IllegalStateException("RecurringOffer does not support Articles with a Product assigned: " + article.getProduct());
			}
		}
		super.checkArticles(articles);
	}
}
