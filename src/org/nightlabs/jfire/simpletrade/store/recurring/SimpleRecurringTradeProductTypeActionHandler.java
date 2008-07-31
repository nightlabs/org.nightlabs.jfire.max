package org.nightlabs.jfire.simpletrade.store.recurring;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler;

/**
 * @author Fitas Amine - fitas at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.recurring.RecurringTradeProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleRecurringTradeProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SimpleRecurringTradeProductTypeActionHandler
extends RecurringTradeProductTypeActionHandler{


	
	/**
	 * @see ProductTypeActionHandler#ProductTypeActionHandler(String, String, Class)
	 */
	public SimpleRecurringTradeProductTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class<? extends SimpleProductType> productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleRecurringTradeProductTypeActionHandler() { }


	@Override
	public  Map<Article, Article> createArticles(RecurredOffer offer, Set<Article> recurringArticles)
	{	
		return null;
	}


}
