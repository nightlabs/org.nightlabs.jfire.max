package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleDeliveryDateSet implements Serializable
{
	private Map<ArticleID, Date> articleID2DeliveryDate;
	private Map<ArticleID, Article> articleID2Article;
	private DeliveryDateMode mode;

	public ArticleDeliveryDateSet(DeliveryDateMode mode) {
		articleID2DeliveryDate = new HashMap<ArticleID, Date>();
		articleID2Article = new HashMap<ArticleID, Article>();
		this.mode = mode;
	}

	public void setArticles(Collection<Article> articles)
	{
		articleID2DeliveryDate.clear();
		articleID2Article.clear();
		for (Article article : articles) {
			ArticleID articleID = (ArticleID) JDOHelper.getObjectId(article);
			Date deliveryDate = null;
			switch (mode) {
				case OFFER:
					deliveryDate = article.getDeliveryDateOffer();
					break;
				case DELIVERY_NOTE:
					deliveryDate = article.getDeliveryDateDeliveryNote();
					break;
			}
			articleID2DeliveryDate.put(articleID, deliveryDate);
			articleID2Article.put(articleID, article);
		}
	}

	public Map<ArticleID, Date> getArticleID2DeliveryDate() {
		return articleID2DeliveryDate;
	}

	public Article getArticle(ArticleID articleID) {
		return articleID2Article.get(articleID);
	}

	public DeliveryDateMode getMode() {
		return mode;
	}
}
