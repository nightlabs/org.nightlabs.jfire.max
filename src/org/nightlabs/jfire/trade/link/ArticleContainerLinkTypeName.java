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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade.link;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeNameID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
		objectIdClass=ArticleContainerLinkTypeNameID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_ArticleContainerLinkTypeName"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups(
		@FetchGroup(
				name=ArticleContainerLinkType.FETCH_GROUP_NAME,
				members={@Persistent(name="texts")}
		)
)
public class ArticleContainerLinkTypeName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String articleContainerLinkTypeID;

	@Join
	@Persistent(table="JFireTrade_ArticleContainerLinkTypeName_texts")
	private Map<String, String> texts;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ArticleContainerLinkTypeName() { }

	public ArticleContainerLinkTypeName(ArticleContainerLinkType articleContainerLinkType)
	{
		this.organisationID = articleContainerLinkType.getOrganisationID();
		this.articleContainerLinkTypeID = articleContainerLinkType.getArticleContainerLinkTypeID();
		texts = new HashMap<String, String>();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return texts;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return String.valueOf(articleContainerLinkTypeID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getArticleContainerLinkTypeID()
	{
		return articleContainerLinkTypeID;
	}
}
