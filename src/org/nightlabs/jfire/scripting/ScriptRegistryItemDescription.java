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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemDescriptionID;

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
@PersistenceCapable(
	objectIdClass=ScriptRegistryItemDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireScripting_ScriptRegistryItemDescription")
@FetchGroups(
	@FetchGroup(
		name="ScriptRegistryItem.description",
		members={@Persistent(name="scriptRegistryItem"), @Persistent(name="texts")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ScriptRegistryItemDescription
extends I18nText
{
	private static final long serialVersionUID = 2756108947806097093L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String scriptRegistryItemType;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String scriptRegistryItemID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireScripting_ScriptRegistryItemDescription_texts",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> texts;

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
		this.texts = new HashMap<String, String>();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return texts;
	}

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
