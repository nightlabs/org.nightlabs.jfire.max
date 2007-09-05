/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Order;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class OrderQuickSearchQuery 
extends AbstractArticleContainerQuickSearchQuery 
{
	@Override
	public Class getArticleContainerClass() {
		return Order.class;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "orderID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "orderPrefixID";
	}
}
