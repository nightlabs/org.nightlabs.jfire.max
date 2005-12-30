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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.store.ProductType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleProductTypeClassGroup
{
	private ArticleSegmentGroup articleSegmentGroup;
//	private String productTypeClassName;
	private Class productTypeClass;

	/**
	 * key: String articlePK<br/>
	 * value: {@link ArticleCarrier} articleCarrier
	 */
	private Map articleCarriers = new HashMap();

//	/**
//	 * Cache for articles, which is normally <code>null</code>!!!
//	 * It will be created and populated by {@link #getArticles()} from
//	 * {@link #articleCarriers}.
//	 */
//	private Set articles = null;

	public ArticleProductTypeClassGroup(ArticleSegmentGroup articleSegmentGroup, Class productTypeClass)
	{
		this.articleSegmentGroup = articleSegmentGroup;
		if (!ProductType.class.isAssignableFrom(productTypeClass))
			throw new IllegalArgumentException("Param productTypeClass \"" + productTypeClass.getName() + "\" does not extend \"" + ProductType.class.getName() + "\"!");

		//this.productTypeClassName = productType.getClass().getName();
		this.productTypeClass = productTypeClass;
	}

	public ArticleSegmentGroup getArticleSegmentGroup()
	{
		return articleSegmentGroup;
	}

//	/**
//	 * @return Returns the productTypeClassName.
//	 */
//	public String getProductTypeClassName()
//	{
//		return productTypeClassName;
//	}
	/**
	 * @return Returns the productTypeClass.
	 */
	public Class getProductTypeClass()
	{
		return productTypeClass;
	}
	public void removeArticle(Article article)
	{
		articleCarriers.remove(article.getPrimaryKey());
		getArticleSegmentGroup().getArticleSegmentGroups()._removeArticle(article);
//		articles = null;
	}
	public void removeArticles(Collection articles)
	{
		for (Iterator it = articles.iterator(); it.hasNext(); )
			removeArticle((Article) it.next());
	}
	/**
	 * Adds a single <tt>Article</tt>.
	 *
	 * @param article The instance to add.
	 * @return Returns the <code>ArticleCarrier</code> which is created by this method
	 *		to wrap the given <code>Article</code>.
	 */
	public ArticleCarrier addArticle(Article article)
	{
		ArticleCarrier articleCarrier = new ArticleCarrier(this, article);
		articleCarriers.put(article.getPrimaryKey(), articleCarrier);
		getArticleSegmentGroup().getArticleSegmentGroups()._addArticleCarrier(articleCarrier);
//		articles = null;
		return articleCarrier;
	}
	/**
	 * Adds a <tt>Collection</tt> of {@link Article}. <tt>null</tt> entries will silently
	 * be ignored.
	 * @param articles The instances to add.
	 * @return Returns a <code>Collection</code> of {@link ArticleCarrier} - the ones that
	 *		have been created by this method in order to wrap the given {@link Article}s.
	 */
	public Collection addArticles(Collection articles)
	{
		Set articleCarriers = new HashSet(articles.size());
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
			if (article != null)
				articleCarriers.add(addArticle(article));
		}
		return articleCarriers;
	}
	/**
	 * @return Returns a <tt>Collection</tt> of {@link Article}.
	 */
	public Collection getArticles()
	{
//		if (articles == null) {
			Set s = new HashSet(articleCarriers.size());
			for (Iterator it = articleCarriers.values().iterator(); it.hasNext();) {
				ArticleCarrier articleCarrier = (ArticleCarrier) it.next();
				s.add(articleCarrier.getArticle());
			}
			return Collections.unmodifiableCollection(s);
//			articles = s;
//		}

//		return articles;
	}

	public Collection getArticleCarriers()
	{
		return Collections.unmodifiableCollection(articleCarriers.values());
	}
}
