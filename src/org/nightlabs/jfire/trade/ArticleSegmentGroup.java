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
import java.util.Iterator;
import java.util.Map;

import org.nightlabs.jfire.store.ProductType;

/**
 * New (2007-06-07): Since this class is used in the client with Jobs, it is now Thread-safe.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleSegmentGroup
{
	private ArticleSegmentGroups articleSegmentGroups;
	private Segment segment;

	/**
	 * key: String productTypeClassName<br/>
	 * value: {@link ArticleProductTypeClassGroup} articleProductTypeGroup 
	 */
	private Map<String, ArticleProductTypeClassGroup> articleProductTypeClassGroups = Collections.synchronizedMap(new HashMap<String, ArticleProductTypeClassGroup>());

	public ArticleSegmentGroup(ArticleSegmentGroups articleSegmentGroups, Segment segment)
	{
		this.articleSegmentGroups = articleSegmentGroups;
		this.segment = segment;
	}

	public ArticleSegmentGroups getArticleSegmentGroups()
	{
		return articleSegmentGroups;
	}

	public synchronized ArticleCarrier addArticle(Article article)
	{
		ArticleProductTypeClassGroup aptg = createArticleProductTypeClassGroup(article.getProductType().getClass());
		return aptg.addArticle(article);
	}

	public synchronized void removeArticle(Article article)
	{
		ArticleProductTypeClassGroup aptg = getArticleProductTypeClassGroup(article.getProductType(), false);
		if (aptg != null)
			aptg.removeArticle(article);
	}

	public synchronized void addArticles(Collection articles)
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article)it.next();
			addArticle(article);
		}
	}

	public synchronized void removeArticles(Collection articles)
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article)it.next();
			removeArticle(article);
		}
	}

	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment()
	{
		return segment;
	}
	/**
	 * @return Returns a <tt>Collection</tt> of {@link ArticleProductTypeClassGroup}.
	 */
	public Collection<ArticleProductTypeClassGroup> getArticleProductTypeGroups()
	{
		return articleProductTypeClassGroups.values();
	}

	/**
	 * @param productType
	 * @return null or a group
	 */
	public ArticleProductTypeClassGroup getArticleProductTypeClassGroup(ProductType productType, boolean throwExceptionIfNotFound)
	{
		return getArticleProductTypeClassGroup(productType.getClass().getName(), throwExceptionIfNotFound);
	}

	public ArticleProductTypeClassGroup getArticleProductTypeClassGroup(String productTypeClassName, boolean throwExceptionIfNotFound)
	{
		ArticleProductTypeClassGroup aptg = (ArticleProductTypeClassGroup)articleProductTypeClassGroups.get(productTypeClassName);
		if (throwExceptionIfNotFound && aptg == null)
			throw new IllegalArgumentException("There is no ArticleProductTypeClassGroup registered for the productTypeClass " + productTypeClassName);

		return aptg;
	}

	/**
	 * Called internally and must return a new instance of <code>ArticleProductTypeClassGroup</code>. You
	 * can override this method, if you want to subclass <code>ArticleProductTypeClassGroup</code>.
	 */
	protected ArticleProductTypeClassGroup _createArticleProductTypeClassGroup(Class productTypeClass)
	{
		return new ArticleProductTypeClassGroup(this, productTypeClass);
	}

	/**
	 * @param productType
	 * @return a newly created group or an old one if already existent before
	 */
	public synchronized ArticleProductTypeClassGroup createArticleProductTypeClassGroup(Class productTypeClass)
	{
		String productTypeClassName = productTypeClass.getName();

		ArticleProductTypeClassGroup aptg = (ArticleProductTypeClassGroup)articleProductTypeClassGroups.get(productTypeClassName);
		if (aptg == null) {
			aptg = _createArticleProductTypeClassGroup(productTypeClass);
			articleProductTypeClassGroups.put(productTypeClassName, aptg);
		}

//		String productTypePK = productType.getPrimaryKey();
//		ArticleProductTypeClassGroup group = (ArticleProductTypeClassGroup) articleProductTypeGroups.get(productTypePK);
//		if (group == null) {
//			group = new ArticleProductTypeClassGroup(productType);
//			articleProductTypeGroups.put(productTypePK, group);
//		}
		return aptg;
	}
}
