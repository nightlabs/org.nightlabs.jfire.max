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

package org.nightlabs.jfire.scripting;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptRegistryItemDescriptionID"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistryItemDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemID"
 *
 * @jdo.fetch-group name="ScriptRegistryItem.description" fields="scriptRegistryItem, texts"
 */
public class ScriptRegistryItemDescription
extends I18nText
{
	private static final long serialVersionUID = 2756108947806097093L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptRegistryItemType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptRegistryItemID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptRegistryItem scriptRegistryItem;

	/**
	 * key: String languageID<br/>
	 * value: String text
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireScripting_ScriptRegistryItemDescription_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 *
	 * @jdo.value-column sql-type="CLOB"
	 */
	protected Map texts;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ScriptRegistryItemDescription()
	{
	}

	public ScriptRegistryItemDescription(ScriptRegistryItem scriptRegistryItem)
	{
		this.scriptRegistryItem = scriptRegistryItem;
		this.organisationID = scriptRegistryItem.getOrganisationID();
		this.scriptRegistryItemType = scriptRegistryItem.getScriptRegistryItemType();
		this.scriptRegistryItemID = scriptRegistryItem.getScriptRegistryItemID();
		this.texts = new HashMap();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map getI18nMap()
	{
		return texts;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return ScriptRegistryItem.getPrimaryKey(organisationID, scriptRegistryItemType, scriptRegistryItemID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getScriptRegistryItemType()
	{
		return scriptRegistryItemType;
	}
	public String getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}
	public ScriptRegistryItem getScriptRegistryItem()
	{
		return scriptRegistryItem;
	}

}
