package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;

/**
 *
 * @author Fitas Amine - fitas at nightlabs dot de
 */

public class DynamicProductTypeRecurringArticleCreator extends ArticleCreator{

	
	private Tariff tariff;
	
	public DynamicProductTypeRecurringArticleCreator(Tariff tariff) {
		super(tariff);
		this.tariff = tariff;
	}


	@Override
	public List<? extends Article> createProductTypeArticles(Trader trader, User user, Offer offer, Segment segment, Collection<? extends ProductType> productTypes)

	{
		List<DynamicProductTypeRecurringArticle> res = new ArrayList<DynamicProductTypeRecurringArticle>(productTypes.size());
		for (ProductType productType : productTypes) {
			res.add(new DynamicProductTypeRecurringArticle(user, offer, segment, DynamicProductTypeRecurringArticle.createArticleID(), productType, tariff));
		}
		return res;

	}

}
