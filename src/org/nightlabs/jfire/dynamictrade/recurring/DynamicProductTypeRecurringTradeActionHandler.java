package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.dynamictrade.DynamicTrader;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductTypeRecurringTradeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @author Fitas Amine - fitas at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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
	{
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


			Article article = dtm.createArticle(segmentID, (OfferID) JDOHelper.getObjectId(offer), productTypeID, recurringArticle.getQuantity(), recurringArticle.getUnitID(), 
					tariffID, recurringArticle.getName(), 
					recurringArticle.getSinglePrice(),  true, true);
			if (article == null)
				throw new IllegalStateException("trader.createArticles(...) created  instead of exactly 1 article!");


			articlesMap.put(recurringArticle, article);

		}

		return articlesMap;

	}

}
