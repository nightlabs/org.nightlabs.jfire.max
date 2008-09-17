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

package org.nightlabs.jfire.trade.id;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;

public interface ArticleContainerID
extends ObjectID
{
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
	
}
