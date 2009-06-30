package org.nightlabs.jfire.trade.deliverydate;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleContainerDeliveryDateDTO
implements Serializable
{
	private ArticleContainerID articleContainerID;
	private ArticleContainer articleContainer;
	private Map<ArticleID, Article> articleID2Article;

	/**
	 * Returns the articleContainerID.
	 * @return the articleContainerID
	 */
	public ArticleContainerID getArticleContainerID() {
		return articleContainerID;
	}
	/**
	 * Sets the articleContainerID.
	 * @param articleContainerID the articleContainerID to set
	 */
	public void setArticleContainerID(ArticleContainerID articleContainerID) {
		this.articleContainerID = articleContainerID;
	}
	/**
	 * Returns the articleContainer.
	 * @return the articleContainer
	 */
	public ArticleContainer getArticleContainer() {
		return articleContainer;
	}
	/**
	 * Sets the articleContainer.
	 * @param articleContainer the articleContainer to set
	 */
	public void setArticleContainer(ArticleContainer articleContainer) {
		this.articleContainer = articleContainer;
	}
	/**
	 * Returns the articleID2Article.
	 * @return the articleID2Article
	 */
	public Map<ArticleID, Article> getArticleID2Article() {
		return articleID2Article;
	}
	/**
	 * Sets the articleID2Article.
	 * @param articleID2Article the articleID2Article to set
	 */
	public void setArticleID2Article(Map<ArticleID, Article> articleID2Article) {
		this.articleID2Article = articleID2Article;
	}

	public Set<ArticleID> getArticleIDs() {
		return articleID2Article.keySet();
	}

	public void addArticle(ArticleID articleID, Article article) {
		articleID2Article.put(articleID, article);
	}

}

