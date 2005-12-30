/*
 * Created on Jun 14, 2005
 */
package org.nightlabs.ipanema.store;

import org.nightlabs.ipanema.store.id.DeliveryNoteID;
import org.nightlabs.ipanema.trade.ArticleContainerException;
import org.nightlabs.ipanema.trade.id.ArticleID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryNoteEditException extends ArticleContainerException
{
	public static int REASON_NESTED_EXCEPTION = 0;
	public static int REASON_ARTICLE_ALREADY_IN_DELIVERY_NOTE = 1;
	public static int REASON_OFFER_NOT_CONFIRMED = 2;
	public static int REASON_ANCHORS_DONT_MATCH = 3;
	public static int REASON_FOREIGN_ORGANISATION = 4;
	public static int REASON_NO_ARTICLES = 5;
	public static int REASON_DELIVERY_NOTE_FINALIZED = 7;
	/**
	 * If an {@link org.nightlabs.ipanema.trade.Article} has been reversed
	 * (i.e. a reversing <code>Article</code> exists), it cannot be added
	 * to a {@link DeliveryNote} anymore.
	 * <p>
	 * This does <b>not</b> mean, that there are no reversed
	 * <code>Article</code>s in <code>DeliveryNote</code>s! An <code>Article</code>
	 * which is already in a <code>DeliveryNote</code> can still be reversed.
	 *
	 * @see #REASON_REVERSING_ARTICLE
	 */
	public static int REASON_REVERSED_ARTICLE = 8;

	/**
	 * A reversing {@link org.nightlabs.ipanema.trade.Article} can only be added to a
	 * {@link DeliveryNote}, if the reversed <code>Article</code> is
	 * in a {@link DeliveryNote}, too. If the reversed article is not yet in a
	 * <code>DeliveryNote</code>, it can neither be added, nor its corresponding
	 * reversing <code>Article</code>.
	 *
	 * @see #REASON_REVERSED_ARTICLE
	 */
	public static int REASON_REVERSING_ARTICLE = 9;

	public DeliveryNoteEditException(int reason)
	{
		this.reason = reason;
	}

	/**
	 * @param message
	 */
	public DeliveryNoteEditException(int reason, String message)
	{
		super(message);
		this.reason = reason;
	}
	/**
	 * @param message
	 * @param cause
	 */
	public DeliveryNoteEditException(int reason, String message, Throwable cause)
	{
		super(message, cause);
		this.reason = reason;
	}
	/**
	 * @param cause
	 */
	public DeliveryNoteEditException(int reason, Throwable cause)
	{
		super(cause);
		this.reason = reason;
	}
	
	public DeliveryNoteEditException(int reason, String message, ArticleID articleID) {
		super(message);
		setArticleID(articleID);
		this.reason = reason;
	}

	public DeliveryNoteEditException(int reason, String message, ArticleID articleID, DeliveryNoteID deliveryNoteID) {
		this(reason, message, articleID);
		setDeliveryNoteID(deliveryNoteID);
	}

	public DeliveryNoteEditException(int reason, Throwable cause, ArticleID articleID) {
		super(cause);
		setArticleID(articleID);
		this.reason = reason;
	}

	public DeliveryNoteEditException(int reason, Throwable cause, ArticleID articleID, DeliveryNoteID deliveryNoteID) {
		this(reason, cause, articleID);
		setDeliveryNoteID(deliveryNoteID);
	}


	private int reason;	

	public int getReason() {
		return reason;
	}
	
	private ArticleID articleID;
	
	public ArticleID getArticleID() {
		return articleID;
	}
	
	public void setArticleID(ArticleID offerItemID) {
		this.articleID = offerItemID;
	}	
	
	private DeliveryNoteID deliveryNoteID;

	public DeliveryNoteID getDeliveryNoteID() {
		return deliveryNoteID;
	}
	public void setDeliveryNoteID(DeliveryNoteID invoiceID) {
		this.deliveryNoteID = invoiceID;
	}
}
