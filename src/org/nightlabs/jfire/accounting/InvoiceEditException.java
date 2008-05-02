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

package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.ArticleContainerException;
import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * Used for errors when creating/editing Invoices.
 * Read the reason-member when catched to determine the error type.
 * The member articleID should be set when an Article for the
 * Invoice is invalid. The member invoiceID should only be set in
 * combination with REASON_ARTICLE_ALREADY_IN_DELIVERY_NOTE.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class InvoiceEditException extends ArticleContainerException
{
	private static final long serialVersionUID = 1L;
	public static int REASON_NESTED_EXCEPTION = 0;
	public static int REASON_ARTICLE_ALREADY_IN_INVOICE = 1;
	public static int REASON_OFFER_NOT_ACCEPTED = 2;
	public static int REASON_ANCHORS_DONT_MATCH = 3;
	public static int REASON_FOREIGN_ORGANISATION = 4;
	public static int REASON_NO_ARTICLES = 5;
	public static int REASON_MULTIPLE_CURRENCIES = 6;
	public static int REASON_INVOICE_FINALIZED = 7;

	/**
	 * If an {@link org.nightlabs.jfire.trade.Article} has been reversed
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
	 * A reversing {@link org.nightlabs.jfire.trade.Article} can only be added to a
	 * {@link DeliveryNote}, if the reversed <code>Article</code> is
	 * in a {@link DeliveryNote}, too. If the reversed article is not yet in a
	 * <code>DeliveryNote</code>, it can neither be added, nor its corresponding
	 * reversing <code>Article</code>.
	 *
	 * @see #REASON_REVERSED_ARTICLE
	 */
	public static int REASON_REVERSING_ARTICLE = 9;

	public InvoiceEditException(int reason) {
		super();
		this.reason = reason;
	}

	public InvoiceEditException(int reason, String message) {
		super(message);
		this.reason = reason;
	}
	
	public InvoiceEditException(int reason, String message, ArticleID articleID) {
		super(message);
		setArticleID(articleID);
		this.reason = reason;
	}

	public InvoiceEditException(int reason, String message, ArticleID articleID, InvoiceID invoiceID) {
		this(reason, message, articleID);
		setInvoiceID(invoiceID);
	}

	public InvoiceEditException(int reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	public InvoiceEditException(int reason, String message, Throwable cause, ArticleID offerItemID) {
		super(message, cause);
		setArticleID(offerItemID);
		this.reason = reason;
	}

	public InvoiceEditException(int reason, String message, Throwable cause, ArticleID articleID, InvoiceID invoiceID) {
		this(reason, message, cause, articleID);
		setInvoiceID(invoiceID);
	}

	public InvoiceEditException(int reason, Throwable cause) {
		super(cause);
		this.reason = reason;
	}

	public InvoiceEditException(int reason, Throwable cause, ArticleID articleID) {
		super(cause);
		setArticleID(articleID);
		this.reason = reason;
	}
	
	public InvoiceEditException(int reason, Throwable cause, ArticleID articleID, InvoiceID invoiceID) {
		this(reason, cause, articleID);
		setInvoiceID(invoiceID);
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
	
	private InvoiceID invoiceID;

	public InvoiceID getInvoiceID() {
		return invoiceID;
	}
	public void setInvoiceID(InvoiceID invoiceID) {
		this.invoiceID = invoiceID;
	}
}
