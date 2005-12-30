/*
 * Created on Mar 8, 2005
 */
package org.nightlabs.jfire.accounting.priceconfig;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IPackagePriceConfig extends IPriceConfig
{
	/**
	 * This method must create a new instance of <tt>ArticlePrice</tt>. This work should be delegated to
	 * {@link PriceConfigUtil#createArticlePrice(IPackagePriceConfig, Article, Price)}.
	 * <p>
	 * Note, that
	 * the ArticlePrice is "empty", means not having any nested prices. They'll be filled by a
	 * later call to {@link #fillArticlePrice(Article)}.
	 * </p>
	 *
	 * @param article The article for which to resolve/calculate a price.
	 */
	ArticlePrice createArticlePrice(Article article);

	/**
	 * This method is called after {@link #createArticlePrice(Article)} asynchronously (when
	 * the {@link Article} is <code>allocationPending</code>). It cannot be called twice (second call fails)
	 * and it should simply delegate to {@link PriceConfigUtil#fillArticlePrice(IPackagePriceConfig, Article)}.
	 *
	 * @param article The <code>Article</code> whose price (obtained by {@link Article#getPrice()})
	 *		shall be filled.
	 */
	void fillArticlePrice(Article article);

	// TODO I think the IPriceConfig must create the ArticlePrice to allow the packaged structure.
	// or maybe it is sufficient to add some methods to IPriceConfig to provide a packagedPrice -
	// or both(?), means there is a PriceConfigUtil class which creates an ArticlePrice and
	// asks all the nested ProductTypes' PriceConfigs...
	// hmmm... I think I have already implemented this?! Marco.

	boolean isDependentOnOffer();
}
