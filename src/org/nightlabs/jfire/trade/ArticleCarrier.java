package org.nightlabs.jfire.trade;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ArticleCarrier
{
	public ArticleCarrier(ArticleProductTypeClassGroup articleProductTypeClassGroup, Article article)
	{
		this.articleProductTypeClassGroup = articleProductTypeClassGroup;
		this.articleSegmentGroup = articleProductTypeClassGroup.getArticleSegmentGroup();
		this.article = article;
	}
	private ArticleProductTypeClassGroup articleProductTypeClassGroup;
	private ArticleSegmentGroup articleSegmentGroup;
	private Article article;
	public ArticleProductTypeClassGroup getArticleProductTypeClassGroup()
	{
		return articleProductTypeClassGroup;
	}
	public ArticleSegmentGroup getArticleSegmentGroup()
	{
		return articleSegmentGroup;
	}
	public Article getArticle()
	{
		return article;
	}
	public void setArticle(Article article)
	{
		this.article = article;
	}
}
