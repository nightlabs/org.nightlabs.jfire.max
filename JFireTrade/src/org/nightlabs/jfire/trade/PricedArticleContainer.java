package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.accounting.Price;

/**
 * This interface should be implemented by any article container that can
 * calculate the price
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 */
public interface PricedArticleContainer extends ArticleContainer{
	/**
	 * The sum of prices of all items
	 * @return
	 */
	Price getPrice();
}