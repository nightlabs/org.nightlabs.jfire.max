/*
 * Created on Apr 8, 2005
 */
package org.nightlabs.jfire.trade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.nightlabs.jfire.store.ProductType;

/**
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
	private Map articleProductTypeClassGroups = new HashMap();

	public ArticleSegmentGroup(ArticleSegmentGroups articleSegmentGroups, Segment segment)
	{
		this.articleSegmentGroups = articleSegmentGroups;
		this.segment = segment;
	}

	public ArticleSegmentGroups getArticleSegmentGroups()
	{
		return articleSegmentGroups;
	}

	public void addArticle(Article article)
	{
		ArticleProductTypeClassGroup aptg = createArticleProductTypeClassGroup(article.getProductType().getClass());
		aptg.addArticle(article);
	}

	public void removeArticle(Article article)
	{
		ArticleProductTypeClassGroup aptg = getArticleProductTypeClassGroup(article.getProductType(), false);
		if (aptg != null)
			aptg.removeArticle(article);
	}

	public void addArticles(Collection articles)
	{
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article)it.next();
			addArticle(article);
		}
	}

	public void removeArticles(Collection articles)
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
	public Collection getArticleProductTypeGroups()
	{
		return articleProductTypeClassGroups.values();
	}

	/**
	 * @param productType
	 * @return null or a group
	 */
	public ArticleProductTypeClassGroup getArticleProductTypeClassGroup(ProductType productType, boolean throwExceptionIfNotFound)
	{
		String productTypeClassName = productType.getClass().getName();

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
	public ArticleProductTypeClassGroup createArticleProductTypeClassGroup(Class productTypeClass)
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
