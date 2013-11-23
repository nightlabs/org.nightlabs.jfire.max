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

package org.nightlabs.jfire.trade;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class FetchGroupsTrade
{
	// The following fetch-groups are used to retrieve the ArticleContainer with its Articles when an editor is opened
	// or when articles have been added/changed. They are not used for the ArticleContainer, when it is changed.
	public static final String FETCH_GROUP_ARTICLE_IN_ORDER_EDITOR = "FetchGroupsTrade.articleInOrderEditor";
	public static final String FETCH_GROUP_ARTICLE_IN_OFFER_EDITOR = "FetchGroupsTrade.articleInOfferEditor";
	public static final String FETCH_GROUP_ARTICLE_IN_INVOICE_EDITOR = "FetchGroupsTrade.articleInInvoiceEditor";
	public static final String FETCH_GROUP_ARTICLE_IN_DELIVERY_NOTE_EDITOR = "FetchGroupsTrade.articleInDeliveryNoteEditor";
	public static final String FETCH_GROUP_ARTICLE_IN_RECEPTION_NOTE_EDITOR = "FetchGroupsTrade.articleInReceptionNoteEditor";

	// I think the above fetch-groups are in mist cases not necessary and maybe we replace them by the following one. Currently, they are combined. Marco.
	public static final String FETCH_GROUP_ARTICLE_IN_ARTICLE_CONTAINER_EDITOR = "FetchGroupsTrade.articleInArticleContainerEditor";

	// The following fetch-groups are used to retrieve the ArticleContainer when it has been changed - in this case it will
	// not detach the articles, since they are managed separately (to improve speed).
	public static final String FETCH_GROUP_ARTICLE_CONTAINER_IN_EDITOR = "FetchGroupsTrade.articleContainerInEditor";

	public static final String FETCH_GROUP_ARTICLE_CROSS_TRADE_REPLICATION= "FetchGroupsTrade.articleCrossTradeReplication";
}
