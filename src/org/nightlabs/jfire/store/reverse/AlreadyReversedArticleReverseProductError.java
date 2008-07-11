package org.nightlabs.jfire.store.reverse;

import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class AlreadyReversedArticleReverseProductError 
extends AbstractReverseProductError 
{
	private ArticleID reversingArticleID = null;
	private ArticleID reversedArticleID = null;
	
	public AlreadyReversedArticleReverseProductError() {
	}

	/**
	 * @param description
	 */
	public AlreadyReversedArticleReverseProductError(String description) {
		super(description);
	}

	/**
	 * Returns the reversingArticleID.
	 * @return the reversingArticleID
	 */
	public ArticleID getReversingArticleID() {
		return reversingArticleID;
	}

	/**
	 * Sets the reversingArticleID.
	 * @param reversingArticleID the reversingArticleID to set
	 */
	public void setReversingArticleID(ArticleID reversingArticleID) {
		this.reversingArticleID = reversingArticleID;
	}

	/**
	 * Returns the reversedArticleID.
	 * @return the reversedArticleID
	 */
	public ArticleID getReversedArticleID() {
		return reversedArticleID;
	}

	/**
	 * Sets the reversedArticleID.
	 * @param reversedArticleID the reversedArticleID to set
	 */
	public void setReversedArticleID(ArticleID reversedArticleID) {
		this.reversedArticleID = reversedArticleID;
	}

}
