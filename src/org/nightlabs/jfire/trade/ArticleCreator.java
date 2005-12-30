/*
 * Created on Sep 12, 2005
 */
package org.nightlabs.jfire.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;

/**
 * A throw-away-instance of this class is used when creating a new {@link org.nightlabs.jfire.trade.Article}.
 * It provides the possibility to create an extended version of <code>Article</code>, if you need
 * additional situation-dependent properties and therefore cannot use the default <code>Article</code>
 * implementation.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleCreator
{
	private Tariff tariff;

	public ArticleCreator(Tariff tariff)
	{
		this.tariff = tariff;
	}

	public Tariff getTariff()
	{
		return tariff;
	}

	/**
	 * This method must create a new <code>Article</code> for a concrete <code>Product<code>. 
	 * Override it, if you want to use extended <code>Article</code>s instead of the
	 * default ones (e.g. if you have more
	 * situation-dependent properties than just the <code>Tariff</code>).
	 * <p>
	 * Usually, this method is a one-liner with sth. like: <code>return new MySpecialArticle(...);</code>
	 * </p>
	 *
	 * @param trader The <code>Trader</code> which is calling this method in one of its <code>createArticles(...)</code> methods.
	 * @param offer The <code>Offer</code> into which the new <code>Article</code> shall be created.
	 * @param segment The <code>Segment</code> into which the new <code>Article</code> shall be created.
	 * @param product The <code>Product</code> for which to create the <code>Article</code>. Note, that this
	 *		<code>Product</code> does not yet have any nested <code>Product</code>s as they are created while allocation.
	 * @return Returns a new (and quite raw) instances of {@link Article} (or descendants). They must NOT yet have an
	 *		{@link ArticlePrice} assigned and they must NOT yet be allocated. This is done by the {@link Trader}
	 *		afterwards (if at all).
	 */
	public List createProductArticles(Trader trader, Offer offer, Segment segment, Collection products)
	{
		List res = new ArrayList(products.size());
		for (Iterator iter = products.iterator(); iter.hasNext();) {
			Product product = (Product) iter.next();
			res.add(new Article(offer, segment, trader.createArticleID(), product, tariff));
		}
		return res;
	}

	/**
	 * This method must create a new <code>Article</code> for a noncommital offer (therefore
	 * only the <code>ProductType</code> is known and no concrete <code>Product</code> yet).
	 * <p>
	 * Usually, this method is a one-liner with sth. like: <code>return new MySpecialArticle(...);</code>
	 * </p>
	 *
	 * @param trader The <code>Trader</code> which is calling this method in one of its <code>createArticles(...)</code> method.
	 * @param offer The <code>Offer</code> into which the new <code>Article</code> shall be created.
	 * @param segment The <code>Segment</code> into which the new <code>Article</code> shall be created.
	 * @param productType The <code>ProductType</code> for which to create this <code>Article</code>.
	 * @return Returns new instances of {@link Article} (or descendants) which might later be linked to concrete <code>Product</code>s.
	 */
	public List createProductTypeArticles(Trader trader, Offer offer, Segment segment, Collection productTypes)
	{
		List res = new ArrayList(productTypes.size());
		for (Iterator iter = productTypes.iterator(); iter.hasNext();) {
			ProductType productType = (ProductType) iter.next();
			res.add(new Article(offer, segment, trader.createArticleID(), productType, tariff));
		}
		return res;
	}
}
