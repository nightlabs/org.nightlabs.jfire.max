package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Order;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class OrderQuery
	extends AbstractArticleContainerQuery<Order>
{
	private static final long serialVersionUID = 1L;

//	@Override
//	public Class getArticleContainerClass() {
//		return Order.class;
//	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "orderID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "orderPrefixID";
	}

	@Override
	protected Class<Order> init()
	{
		return Order.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuffer filter)
	{
		// no additional fields needed yet 
	}
}
