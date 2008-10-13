package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.Map;
import java.util.Set;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManager;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerUtil;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Segment;
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

	private DynamicTradeManager getDynamicTradeManager() {
		try {
			return DynamicTradeManagerUtil.getHome().create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public Map<Article, Article> createArticles(RecurredOffer offer,
			Set<Article> recurringArticles, Segment segment)
			throws ModuleException {
//	
//		Map<Article, Article> articlesMap=  new HashMap<Article, Article>();
//
//		Article recurredArticle;
//
//		PersistenceManager pm = getPersistenceManager();
//		DynamicTradeManager dtm = getDynamicTradeManager();
//		Trader trader = Trader.getTrader(pm);
//		Store store = Store.getStore(pm);
//		User user = SecurityReflector.getUserDescriptor().getUser(pm);
//
//		ProductType pt = null;
//		
//		for (Iterator<Article> it = recurringArticles.iterator(); it.hasNext(); ) 
//		{	
//			recurredArticle = it.next();
//			pt = recurredArticle.getProductType();
//
//			DynamicProductType productType = (DynamicProductType)pt;		
//
//			
//			dtm.createRecurringArticle(segmentID, offerID, productTypeID, quantity, unitID, tariffID, productName, fetchGroups, maxFetchDepth)
//
//			dtm.createRecurringArticle(segment , offer,pt
//			Article ar =  trader.createArticles(user, offer, segment, products,
//					new ArticleCreator(recurredArticle.getTariff()), true, true);
////	        dtm.createArticle(segmentID, offerID, productTypeID, quantity, unitID, tariffID, productName, singlePrice, allocate, allocateSynchronously, fetchGroups, maxFetchDepth)
////	        if (ar == null)
////				throw new IllegalStateException("trader.createArticles(...) created  instead of exactly 1 article!");
//				
////			articlesMap.put(recurredArticle, ar);
//
//		}
//
//		return articlesMap;

		return null;

	}

}
