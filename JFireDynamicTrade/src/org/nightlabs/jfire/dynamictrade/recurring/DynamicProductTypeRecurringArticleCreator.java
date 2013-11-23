package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.dynamictrade.DynamicProductInfo;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;

/**
 * @author Fitas Amine - fitas at nightlabs dot de
 */
public class DynamicProductTypeRecurringArticleCreator extends ArticleCreator
{
	private DynamicProductInfo dynamicProductInfo;
	private Tariff tariff;
	
	public DynamicProductTypeRecurringArticleCreator(Tariff tariff, DynamicProductInfo dynamicProductInfo) {
		super(tariff);
		this.tariff = tariff;
		this.dynamicProductInfo = dynamicProductInfo;
	}

	@Override
	public List<? extends Article> createProductTypeArticles(Trader trader, User user, Offer offer, Segment segment, Collection<? extends ProductType> productTypes)
	{
		List<DynamicProductTypeRecurringArticle> res = new ArrayList<DynamicProductTypeRecurringArticle>(productTypes.size());
		for (ProductType productType : productTypes) {
			DynamicProductTypeRecurringArticle article = new DynamicProductTypeRecurringArticle(user, offer, segment, DynamicProductTypeRecurringArticle.createArticleID(), productType, tariff);
			article.getName().copyFrom(dynamicProductInfo.getName());
			article.setQuantity(dynamicProductInfo.getQuantity());
			article.setSinglePrice(dynamicProductInfo.getSinglePrice());
			article.setUnit(dynamicProductInfo.getUnit());
			res.add(article);
		}
		return res;

	}
}
