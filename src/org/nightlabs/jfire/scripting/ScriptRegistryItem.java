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

package org.nightlabs.jfire.scripting;

import java.io.Serializable;

import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptRegistryItemID"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistryItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemNature, scriptRegistryItemID"
 */
public class ScriptRegistryItem
		implements Serializable
{
	private static final long serialVersionUID = 9221181132208442543L;

	protected static final String SCRIPT_REGISTRY_ITEM_NATURE_CATEGORY = "category";
	protected static final String SCRIPT_REGISTRY_ITEM_NATURE_SCRIPT = "script";

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
	 * This is either "{@value ScriptRegistryItem#SCRIPT_REGISTRY_ITEM_NATURE_CATEGORY}" or "{@value ScriptRegistryItem#SCRIPT_REGISTRY_ITEM_NATURE_SCRIPT}".
	 * Use the constants!
	 *
	 * @see ScriptRegistryItem#SCRIPT_REGISTRY_ITEM_NATURE_CATEGORY
	 * @see ScriptRegistryItem#SCRIPT_REGISTRY_ITEM_NATURE_SCRIPT
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="10"
	 */
	private String scriptRegistryItemNature;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptRegistryItemID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptCategory parent;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptParameterSet parameterSet;

//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="ScriptParameter"
//	 *		dependent="true"
//	 *		table="JFireScripting_ScriptRegistryItem_parameters"
//	 *
//	 * @jdo.join
//	 */
//	private Map<String, ScriptParameter> parameters;

	/**
	 * @deprecated Only for JDO! 
	 */
	protected ScriptRegistryItem() { }

	public ScriptRegistryItem(String organisationID, String scriptRegistryItemType, String scriptRegistryItemNature, String scriptRegistryItemID)
	{
		this.organisationID = organisationID;
		this.scriptRegistryItemType = scriptRegistryItemType;
		this.scriptRegistryItemNature = scriptRegistryItemNature;
		this.scriptRegistryItemID = scriptRegistryItemID;
	}

	public static String getPrimaryKey(String organisationID, String scriptRegistryItemType, String scriptRegistryItemNature, String scriptRegistryItemID)
	{
		return organisationID + '/' + scriptRegistryItemType + '/' + scriptRegistryItemNature  + '/' + scriptRegistryItemID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getScriptRegistryItemType()
	{
		return scriptRegistryItemType;
	}
	/**
	 * The nature serves as distinction between scripts and categories in order to separate
	 * their namespaces. This makes it easier to name the scripts whose ids can then be
	 * directly used as variables.
	 *
	 * @return Returns one of {@link #SCRIPT_REGISTRY_ITEM_NATURE_CATEGORY} or {@link #SCRIPT_REGISTRY_ITEM_NATURE_SCRIPT} 
	 */
	public String getScriptRegistryItemNature()
	{
		return scriptRegistryItemNature;
	}
	public String getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

	protected ScriptCategory getParent()
	{
		return parent;
	}
	protected void setParent(ScriptCategory parent)
	{
		this.parent = parent;
	}

	public ScriptParameterSet getParameterSet()
	{
		return parameterSet;
	}

	public void setParameterSet(ScriptParameterSet parameterSet)
	{
		this.parameterSet = parameterSet;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof ScriptRegistryItem))
			return false;

		ScriptRegistryItem other = (ScriptRegistryItem) obj;

		return
				Utils.equals(this.organisationID,           other.organisationID) &&
				Utils.equals(this.scriptRegistryItemType,   other.scriptRegistryItemType) &&
				Utils.equals(this.scriptRegistryItemNature, other.scriptRegistryItemNature) &&
				Utils.equals(this.scriptRegistryItemID,     other.scriptRegistryItemID);
	}

	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(organisationID) ^
				Utils.hashCode(scriptRegistryItemType) ^
				Utils.hashCode(scriptRegistryItemNature) ^
				Utils.hashCode(scriptRegistryItemID);
	}
}
