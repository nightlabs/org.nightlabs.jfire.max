/*
 * Created on Apr 8, 2005
 */
package org.nightlabs.ipanema.trade;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.ipanema.trade.id.ArticleID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleSegmentGroups
{
	/**
	 * key: String segmentPK<br/>
	 * value: {@link ArticleSegmentGroup} articleSegmentGroup
	 */
	private Map articleSegmentGroups = new HashMap();

	/**
	 * key: {@link org.nightlabs.ipanema.trade.id.ArticleID} articleID<br/>
	 * value: {@link ArticleCarrier} articleCarrier
	 */
	private Map articleCarriers = new HashMap();

	public boolean containsArticle(ArticleID articleID) {
		return articleCarriers.containsKey(articleID);
	}

	/**
	 * @param throwExceptionIfNotFound Whether to throw an exception or to return <code>null</code>.
	 * @return Returns <code>null</code>, if not found and <code>throwExceptionIfNotFound</code>.
	 */
	protected ArticleCarrier getArticleCarrier(ArticleID articleID, boolean throwExceptionIfNotFound)
	{
		ArticleCarrier res = (ArticleCarrier) articleCarriers.get(articleID);

		if (throwExceptionIfNotFound && res == null)
			throw new IllegalArgumentException("Could not find an ArticleCarrier for " + articleID);

		return res;
	}

//	private Set articles = null;

	/**
	 * @param articleContainer This might additionally implement the interface
	 * {@link SegmentContainer} to provide {@link Segment}s even if no {@link Article}
	 * exists.
	 */
	public ArticleSegmentGroups(ArticleContainer articleContainer)
	{
		initWithArticleContainer(articleContainer);

		if (articleContainer instanceof SegmentContainer)
			initWithSegmentContainer((SegmentContainer)articleContainer);
	}

	protected void initWithSegmentContainer(SegmentContainer segmentContainer)
	{
		for (Iterator it = segmentContainer.getSegments().iterator(); it.hasNext(); ) {
			Segment segment = (Segment) it.next();

			String segmentPK = segment.getPrimaryKey();
			ArticleSegmentGroup asg = (ArticleSegmentGroup) articleSegmentGroups.get(segmentPK);
			if (asg == null) {
				asg = _createArticleSegmentGroup(segment);
				articleSegmentGroups.put(segmentPK, asg);
			}
		}
	}

	/**
	 * Called internally and must return a new instance of <code>ArticleSegmentGroup</code>. You
	 * can override this method, if you want to subclass <code>ArticleSegmentGroup</code>.
	 */
	protected ArticleSegmentGroup _createArticleSegmentGroup(Segment segment)
	{
		return new ArticleSegmentGroup(this, segment);
	}

	protected void initWithArticleContainer(ArticleContainer articleContainer)
	{
		for (Iterator it = articleContainer.getArticles().iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();

			String segmentPK = article.getSegment().getPrimaryKey();
			ArticleSegmentGroup asg = (ArticleSegmentGroup) articleSegmentGroups.get(segmentPK);
			if (asg == null) {
				asg = _createArticleSegmentGroup(article.getSegment());
				articleSegmentGroups.put(segmentPK, asg);
			}

			// we want to have the same instance of Segment if they're equal - deduplicate
			// ...just in case the detach bug still exists
			if (article.getSegment() != asg.getSegment())
				article.setSegment(asg.getSegment());

			asg.addArticle(article);
		}
	}

	/**
	 * @return Returns a <tt>Collection</tt> of {@link ArticleSegmentGroup}.
	 */
	public Collection getArticleSegmentGroups()
	{
		return articleSegmentGroups.values();
	}

	public Set getArticles()
	{
//		if (articles == null) {
			Set s = new HashSet();

			for (Iterator it = articleCarriers.values().iterator(); it.hasNext();) {
				ArticleCarrier articleCarrier = (ArticleCarrier) it.next();
				s.add(articleCarrier.getArticle());
			}
			return Collections.unmodifiableSet(s);
//			articles = s;
//		}
//		return articles;
	}

	/**
	 * This method is called by {@link ArticleProductTypeClassGroup#addArticle(Article)}. This guarantees
	 * that {@link #getArticles()} returns a correct <code>Set</code>.
	 */
	protected void _addArticleCarrier(ArticleCarrier articleCarrier)
	{
		articleCarriers.put(JDOHelper.getObjectId(articleCarrier.getArticle()), articleCarrier);
//		articles = null;
	}

	/**
	 * This method is called by {@link ArticleProductTypeClassGroup#removeArticle(Article)}. This guarantees
	 * that {@link #getArticles()} returns a correct <code>Set</code>. 
	 */
	protected void _removeArticle(Article article)
	{
		articleCarriers.remove(JDOHelper.getObjectId(article));
//		articles = null;
	}
}
