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

import java.util.Collection;
import java.util.Date;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface ArticleContainer
{
	AnchorID getCustomerID();
	AnchorID getVendorID();

	LegalEntity getCustomer();
	LegalEntity getVendor();

	/**
	 * @return the organisation that issued the ArticleContainer - usually the vendor organisation. This is the first part
	 * of the composite primary key.
	 * @see #getArticleContainerIDPrefix()
	 * @see #getArticleContainerID()
	 */
	String getOrganisationID();

	/**
	 * @return the prefix for the local ID, which can be used to have a namespace per year or per topic. This is the 2nd part
	 * of the composite primary key.
	 * @see #getOrganisationID()
	 * @see #getArticleContainerID()
	 */
	String getArticleContainerIDPrefix();

	/**
	 * @return the id within the scope of the organisationID and the prefix. This is the 3rd part of the composite primary key.
	 * @see #getOrganisationID()
	 * @see #getArticleContainerIDPrefix()
	 */
	long getArticleContainerID();

	/**
	 * @return the result of {@link ObjectIDUtil#longObjectIDFieldToString(long)} applied to {@link #getArticleContainerID()}
	 */
	String getArticleContainerIDAsString();

	/**
	 * @return A <tt>Collection</tt> of {@link Article}
	 */
	Collection<Article> getArticles();

	/**
	 * adds an {@link Article} to the ArticleContainer
	 * @param article the article to add
	 * @throws ArticleContainerException
	 */
	void addArticle(Article article)
	throws ArticleContainerException;

	/**
	 * removes an {@link Article} from the ArticleContainer
	 * @param article the article to remove
	 * @throws ArticleContainerException
	 */
	void removeArticle(Article article)
	throws ArticleContainerException;

	/**
	 * @return Returns when this <code>ArticleContainer</code> has been created.
	 */
	Date getCreateDT();

	/**
	 * @return Returns the <code>User</code> who is responsible for creation of this <code>ArticleContainer</code>.
	 */
	User getCreateUser();
	
	/**
	 * Returns the number of the articles in this article container.	 *
	 * @return the number of the articles in this article container.
	 */
	int getArticleCount();
}
