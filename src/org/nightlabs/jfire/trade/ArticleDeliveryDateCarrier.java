package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleDeliveryDateCarrier
implements Serializable
{
	private ArticleID articleID;
	private Date deliveryDate;
	private DeliveryDateMode mode;
	// must be transient so that it will not be serialized when transmitted
//	private transient Article article;

//	protected ArticleDeliveryDateCarrier() {}

//	public ArticleDeliveryDateCarrier(ArticleID articleID, Article article) {
//		this.articleID = articleID;
//		this.article = article;
//	}
//
//	public ArticleDeliveryDateCarrier(ArticleID articleID, Date deliveryDate, Article article, DeliveryDateMode mode) {
//		this.articleID = articleID;
//		this.deliveryDate = deliveryDate;
//		this.mode = mode;
//		this.article = article;
//	}

	public ArticleDeliveryDateCarrier(ArticleID articleID, Date deliveryDate, DeliveryDateMode mode) {
		this.articleID = articleID;
		this.deliveryDate = deliveryDate;
		this.mode = mode;
	}

	/**
	 * Returns the deliveryDate.
	 * @return the deliveryDate
	 */
	public Date getDeliveryDate() {
		return deliveryDate;
	}

	/**
	 * Sets the deliveryDate.
	 * @param deliveryDate the deliveryDate to set
	 */
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	/**
	 * Returns the articleID.
	 * @return the articleID
	 */
	public ArticleID getArticleID() {
		return articleID;
	}

	/**
	 * Returns the mode.
	 * @return the mode
	 */
	public DeliveryDateMode getMode() {
		return mode;
	}
//
//	/**
//	 * Sets the mode.
//	 * @param mode the mode to set
//	 */
//	public void setMode(DeliveryDateMode mode) {
//		this.mode = mode;
//	}

//	/**
//	 * Returns the article.
//	 * @return the article
//	 */
//	public Article getArticle() {
//		return article;
//	}

}
