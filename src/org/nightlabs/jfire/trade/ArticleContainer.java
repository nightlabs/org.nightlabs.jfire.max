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

	/**
	 * @return Returns the organisation that issued the ArticleContainer - usually the vendor organisation.
	 */
	String getOrganisationID();

	/**
	 * @return A <tt>Collection</tt> of {@link Article}
	 */
	Collection getArticles();

	void addArticle(Article article)
	throws ArticleContainerException;

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
}
