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


	public Collection<? extends Article> createArticles(User user, Offer offer, Segment segment,
			Collection<ProductType> productTypes, ArticleCreator articleCreator)throws ModuleException
			{
		if (!segment.getOrder().equals(offer.getOrder()))
			throw new IllegalArgumentException("segment.order != offer.order :: " + segment.getOrder().getPrimaryKey() + " != " + offer.getOrder().getPrimaryKey());

		PersistenceManager pm = getPersistenceManager();

		Trader trader = Trader.getTrader(pm);
		Collection<? extends Article> articles = articleCreator.createProductTypeArticles(trader, user, offer,
				segment, productTypes);

		for (Article article : articles) {
			article.createArticleLocal(user);
		}
		// WORKAROUND begin
		articles = pm.makePersistentAll(articles);
		// WORKAROUND end

		offer.addArticles(articles);
		// create the Articles' prices
		for (Article article : articles) {
			IPackagePriceConfig packagePriceConfig = article.getProductType()
			.getPackagePriceConfig();
			article.setPrice(packagePriceConfig.createArticlePrice(article));
		}

		trader.validateOffer(offer, false);

		return articles;
			}


}
