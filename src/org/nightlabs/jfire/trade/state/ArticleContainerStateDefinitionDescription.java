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

package org.nightlabs.jfire.trade.state;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.ArticleContainerStateDefinitionDescriptionID"
 *		detachable="true"
 *		table="JFireTrade_ArticleContainerStateDefinitionDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, articleContainerStateDefinitionID"
 *
 * @jdo.fetch-group name="ArticleContainerStateDefinition.description" fields="articleContainerStateDefinition, descriptions"
 */
public class ArticleContainerStateDefinitionDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String articleContainerStateDefinitionClass;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String articleContainerStateDefinitionID;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_ArticleContainerStateDefinitionDescription_descriptions"
	 *
	 * @jdo.key-column length="5"
	 * @jdo.value-column jdbc-type="LONGVARCHAR"
	 *
	 * @jdo.join
	 */
	protected Map descriptions = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ArticleContainerStateDefinition articleContainerStateDefinition;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ArticleContainerStateDefinitionDescription()
	{
	}

	public ArticleContainerStateDefinitionDescription(ArticleContainerStateDefinition articleContainerStateDefinition)
	{
		this.organisationID = articleContainerStateDefinition.getOrganisationID();
		this.articleContainerStateDefinitionClass = articleContainerStateDefinition.getStateDefinitionClass();
		this.articleContainerStateDefinitionID = articleContainerStateDefinition.getStateDefinitionID();
		this.articleContainerStateDefinition = articleContainerStateDefinition;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return ArticleContainerStateDefinition.getPrimaryKey(organisationID, articleContainerStateDefinitionClass, articleContainerStateDefinitionID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getArticleContainerStateDefinitionID()
	{
		return articleContainerStateDefinitionID;
	}

	public ArticleContainerStateDefinition getArticleContainerStateDefinition()
	{
		return articleContainerStateDefinition;
	}

}
