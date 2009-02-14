package org.nightlabs.jfire.voucher.recurring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * @author Fitas Amine - fitas at nightlabs dot de
 *
 * the {@link VoucherTypeRecurringTradeActionHandler} extends the abstract class {@link RecurringTradeProductTypeActionHandler}
 * it creates articles for the voucher type. 
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireSimpleTrade_VoucherRecurringTradeProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */

public class VoucherTypeRecurringTradeActionHandler 
extends	RecurringTradeProductTypeActionHandler
{

	
	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public VoucherTypeRecurringTradeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class<? extends VoucherType> productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherTypeRecurringTradeActionHandler() { }



	@Override
	public  Map<Article, Article> createArticles(RecurredOffer offer, Set<Article> recurringArticles,Segment segment)
	{	
		Map<Article, Article> articlesMap=  new HashMap<Article, Article>();

		Article article;

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);
		Store store = Store.getStore(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		ProductType pt = null;

		for (Iterator<Article> it = recurringArticles.iterator(); it.hasNext(); ) 
		{	
			article = it.next();
			pt = article.getProductType();

			VoucherType productType = (VoucherType)pt;		

			// find  Products
			Collection<? extends Product> products = store.findProducts(user, productType, null, null); // we create exactly one => no NestedProductTypeLocal needed
			if (products.size() != 1)
				throw new IllegalStateException("store.findProducts(...) created " + products.size() + " instead of exactly 1 product!");

			Collection<? extends Article> articles=  trader.createArticles(user, offer, segment, products,
					new ArticleCreator(null), true, true);
			if (articles.size() != 1)
				throw new IllegalStateException("trader.createArticles(...) created " + articles.size() + " instead of exactly 1 article!");

			articlesMap.put(article, articles.iterator().next());
		}

		return articlesMap;
	}


	
	
}
