/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
	 * Usually, this method has only a few lines with a loop creating one {@link Article} for each given {@link Product}.
	 * </p>
	 *
	 * @param trader The <code>Trader</code> which is calling this method in one of its <code>createArticles(...)</code> methods.
	 * @param offer The <code>Offer</code> into which the new <code>Article</code>s shall be created.
	 * @param segment The <code>Segment</code> into which the new <code>Article</code>s shall be created.
	 * @param products The {@link Product}s for which to create the <code>Article</code>s. Note, that this
	 *		<code>Product</code>s do not yet have any nested <code>Product</code>s as they are created while allocation.
	 * @return Returns new (and quite raw) instances of {@link Article} (or descendants). They must NOT yet have an
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
	 * Usually, this method has only a few lines with a loop creating one {@link Article} for each given {@link ProductType}.
	 * </p>
	 *
	 * @param trader The <code>Trader</code> which is calling this method in one of its <code>createArticles(...)</code> method.
	 * @param offer The <code>Offer</code> into which the new <code>Article</code>s shall be created.
	 * @param segment The <code>Segment</code> into which the new <code>Article</code>s shall be created.
	 * @param productTypes The {@link ProductType}s for which to create <code>Article</code>s.
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
