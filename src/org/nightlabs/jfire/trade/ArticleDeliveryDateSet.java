package org.nightlabs.jfire.trade;

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
public class ArticleDeliveryDateSet {

//	public static enum DeliveryDateMode {
//		/**
//		 * show the deliveryDateOffer of the article
//		 */
//		OFFER,
//		/**
//		 * show the deliveryDateDeliveryNote of the article
//		 */
//		DELIVERY_NOTE
//	}

	private Map<ArticleID, Date> articleID2DeliveryDate;
	private DeliveryDateMode mode;

	public ArticleDeliveryDateSet(Map<ArticleID, Date> articleID2DeliveryDate, DeliveryDateMode mode) {
		this.articleID2DeliveryDate = articleID2DeliveryDate;
		this.mode = mode;
	}

	public ArticleDeliveryDateSet(DeliveryDateMode mode) {
		articleID2DeliveryDate = new HashMap<ArticleID, Date>();
		this.mode = mode;
	}

	public void setArticles(Collection<Article> articles)
	{
		articleID2DeliveryDate.clear();
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
		}
	}

}
