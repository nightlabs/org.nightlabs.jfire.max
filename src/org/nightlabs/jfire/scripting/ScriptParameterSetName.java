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

import org.nightlabs.annotation.Implement;
import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptParameterSetNameID"
 *		detachable="true"
 *		table="JFireScripting_ScriptParameterSetName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptParameterSetID"
 *
 * @jdo.fetch-group name="ScriptParameterSet.name" fields="scriptParameterSet, names"
 */
public class ScriptParameterSetName
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
	 */
	private long scriptParameterSetID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptParameterSet scriptParameterSet;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireScripting_ScriptParameterSetName_names"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected ScriptParameterSetName()
	{
	}

	public ScriptParameterSetName(ScriptParameterSet scriptParameterSet)
	{
		this.scriptParameterSet = scriptParameterSet;
		this.organisationID = scriptParameterSet.getOrganisationID();
		this.scriptParameterSetID = scriptParameterSet.getScriptParameterSetID();
	}

	@Implement
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Implement
	protected String getFallBackValue(String languageID)
	{
		return ScriptParameterSet.getPrimaryKey(organisationID, scriptParameterSetID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	public ScriptParameterSet getScriptParameterSet() {
		return scriptParameterSet;
	}
	
	public long getScriptParameterSetID() {
		return scriptParameterSetID;
	}
}
