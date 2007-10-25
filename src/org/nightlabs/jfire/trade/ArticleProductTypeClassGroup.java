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
 * New (2007-06-07): Since this class is used in the client with Jobs, it is now Thread-safe.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleProductTypeClassGroup
{
	private ArticleSegmentGroup articleSegmentGroup;
//	private String productTypeClassName;
	private Class<? extends ProductType> productTypeClass;

	/**
	 * key: String articlePK<br/>
	 * value: {@link ArticleCarrier} articleCarrier
	 */
	private Map<String, ArticleCarrier> articleCarriers = Collections.synchronizedMap(new HashMap<String, ArticleCarrier>());

//	/**
//	 * Cache for articles, which is normally <code>null</code>!!!
//	 * It will be created and populated by {@link #getArticles()} from
//	 * {@link #articleCarriers}.
//	 */
//	private Set articles = null;

	public ArticleProductTypeClassGroup(ArticleSegmentGroup articleSegmentGroup, Class<? extends ProductType> productTypeClass)
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
	public Class<? extends ProductType> getProductTypeClass()
	{
		return productTypeClass;
	}
	public void removeArticle(Article article)
	{
		articleCarriers.remove(article.getPrimaryKey());
		getArticleSegmentGroup().getArticleSegmentGroups()._removeArticle(article);
//		articles = null;
	}
	public void removeArticles(Collection<? extends Article> articles)
	{
		for (Iterator<? extends Article> it = articles.iterator(); it.hasNext(); )
			removeArticle(it.next());
	}
	/**
	 * Adds a single <tt>Article</tt>.
	 *
	 * @param article The instance to add.
	 * @param filterExisting Whether or not to return <code>null</code> if the passed article already exists.
	 * @return Returns the <code>ArticleCarrier</code> which is created by this method
	 *		to wrap the given <code>Article</code>. If the article already exists, this method replaces the article
	 *		referenced by the existing ArticleCarrier. If <code>filterExisting</code> is <code>false</code>, it then returns the existing
	 *		<code>ArticleCarrier</code>, if <code>filterExisting</code> is <code>true</code>, it returns <code>null</code>.
	 */
	public synchronized ArticleCarrier addArticle(Article article, boolean filterExisting)
	{
		ArticleCarrier articleCarrier = articleCarriers.get(article.getPrimaryKey());
		if (articleCarrier != null) {
			articleCarrier.setArticle(article);
			if (filterExisting)
				return null;

			return articleCarrier;
		}

		articleCarrier = new ArticleCarrier(this, article);
		articleCarriers.put(article.getPrimaryKey(), articleCarrier);
		getArticleSegmentGroup().getArticleSegmentGroups()._addArticleCarrier(articleCarrier);
//		articles = null;
		return articleCarrier;
	}
	/**
	 * Adds a <tt>Collection</tt> of {@link Article}. <tt>null</tt> entries will silently
	 * be ignored.
	 * @param articles The instances to add.
	 * @param filterExisting Whether or not to include previously existing ArticleCarriers (or only newly created ones).
	 * @return Returns a <code>Collection</code> of {@link ArticleCarrier} - the ones that
	 *		have been created by this method in order to wrap the given {@link Article}s. If <code>filterExisting</code>
	 *		is <code>true</code>, the result will not contain <code>ArticleCarrier</code>s whose {@link Article}s already existed
	 *		before. 
	 */
	public Collection<ArticleCarrier> addArticles(Collection<? extends Article> articles, boolean filterExisting)
	{
		Set<ArticleCarrier> articleCarriers = new HashSet<ArticleCarrier>(articles.size());
		for (Iterator<? extends Article> it = articles.iterator(); it.hasNext(); ) {
			Article article = it.next();
			if (article != null) {
				ArticleCarrier articleCarrier = addArticle(article, filterExisting);
				if (articleCarrier != null)
					articleCarriers.add(articleCarrier);
			}
		}
		return articleCarriers;
	}
	/**
	 * @return Returns a <tt>Collection</tt> of {@link Article}.
	 */
	public Collection<Article> getArticles()
	{
		Set<Article> s = new HashSet<Article>(articleCarriers.size());
		for (Iterator<ArticleCarrier> it = articleCarriers.values().iterator(); it.hasNext();) {
			ArticleCarrier articleCarrier = it.next();
			s.add(articleCarrier.getArticle());
		}
		return Collections.unmodifiableCollection(s);
	}

	public Collection<ArticleCarrier> getArticleCarriers(Collection<Article> articles)
	{
		Set<ArticleCarrier> res = new HashSet<ArticleCarrier>(articles.size());
		for (Article article : articles) {
			ArticleCarrier articleCarrier = articleCarriers.get(article.getPrimaryKey());
			if (articleCarrier != null)
				res.add(articleCarrier);
		}
		return res;
	}

	public Collection<ArticleCarrier> getArticleCarriers()
	{
		return Collections.unmodifiableCollection(articleCarriers.values());
	}
}
