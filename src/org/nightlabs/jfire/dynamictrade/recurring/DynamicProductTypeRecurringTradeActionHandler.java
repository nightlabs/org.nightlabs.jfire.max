package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.dynamictrade.DynamicTrader;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler;

/**
 *
 * @author Fitas Amine - fitas at nightlabs dot de
 */

public class DynamicProductTypeRecurringTradeActionHandler extends RecurringTradeProductTypeActionHandler
{

	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public DynamicProductTypeRecurringTradeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class<? extends DynamicProductType> productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTypeRecurringTradeActionHandler() { }


	@Override
	public Map<Article, Article> createArticles(RecurredOffer offer,
			Set<Article> recurringArticles, Segment segment)
			throws ModuleException {

		Map<Article, Article> articlesMap=  new HashMap<Article, Article>();

		DynamicProductTypeRecurringArticle recurringArticle;

		PersistenceManager pm = getPersistenceManager();
		DynamicTrader dtm = DynamicTrader.getDynamicTrader(pm);

		for (Iterator<Article> it = recurringArticles.iterator(); it.hasNext(); ) 
		{	
			recurringArticle = (DynamicProductTypeRecurringArticle)it.next();

			ProductTypeID productTypeID = (ProductTypeID)JDOHelper.getObjectId(recurringArticle.getProductType());		
			SegmentID segmentID = (SegmentID) JDOHelper.getObjectId(segment);
			TariffID tariffID = (TariffID) JDOHelper.getObjectId(recurringArticle.getTariff());


			Article article = dtm.createArticle(segmentID, recurringArticle.getOfferID(), productTypeID, recurringArticle.getQuantity(), recurringArticle.getUnitID(), 
					tariffID, recurringArticle.getName(), 
					recurringArticle.getSinglePrice(),  true, false, 
					new String[] {
				FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			if (article == null)
				throw new IllegalStateException("trader.createArticles(...) created  instead of exactly 1 article!");


			articlesMap.put(recurringArticle, article);

		}

		return articlesMap;

	}

}
