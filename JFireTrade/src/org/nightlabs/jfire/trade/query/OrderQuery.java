package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Order;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class OrderQuery
	extends AbstractArticleContainerQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getArticleContainerIDMemberName() {
		return "orderID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "orderIDPrefix";
	}

	@Override
	protected Class<Order> initCandidateClass()
	{
		return Order.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuilder filter)
	{
		// no additional fields needed yet
	}
}
