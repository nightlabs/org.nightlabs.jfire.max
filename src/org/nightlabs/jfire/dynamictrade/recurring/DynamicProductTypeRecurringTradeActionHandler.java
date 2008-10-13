package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.Map;
import java.util.Set;

import org.nightlabs.ModuleException;
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

	
	@Override
	public Map<Article, Article> createArticles(RecurredOffer offer,
			Set<Article> recurringArticles, Segment segment)
			throws ModuleException {
		// TODO Auto-generated method stub
		return null;
	}

}
