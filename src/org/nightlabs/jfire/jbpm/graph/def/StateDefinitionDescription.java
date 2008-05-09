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

package org.nightlabs.jfire.jbpm.graph.def;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionDescriptionID"
 *		detachable="true"
 *		table="JFireJbpm_StateDefinitionDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="processDefinitionOrganisationID, processDefinitionID, stateDefinitionOrganisationID, stateDefinitionID"
 *
 * @jdo.fetch-group name="StateDefinition.description" fields="stateDefinition, descriptions"
 */
public class StateDefinitionDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 * 
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@SuppressWarnings("unused")
	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 * 
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@SuppressWarnings("unused")
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 * 
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@SuppressWarnings("unused")
	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String stateDefinitionID;

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
	 *		table="JFireJbpm_StateDefinitionDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.key-column length="5"
	 * @jdo.value-column jdbc-type="LONGVARCHAR"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private StateDefinition stateDefinition;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected StateDefinitionDescription()
	{
	}

	public StateDefinitionDescription(StateDefinition stateDefinition)
	{
		this.processDefinitionOrganisationID = stateDefinition.getProcessDefinitionOrganisationID();
		this.processDefinitionID = stateDefinition.getProcessDefinitionID();
		this.stateDefinitionOrganisationID = stateDefinition.getStateDefinitionOrganisationID();
		this.stateDefinitionID = stateDefinition.getStateDefinitionID();
		this.stateDefinition = stateDefinition;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return ""; // StateDefinition.getPrimaryKey(processDefinitionOrganisationID, processDefinitionID, stateDefinitionOrganisationID, stateDefinitionID);
	}

	public String getStateDefinitionID()
	{
		return stateDefinitionID;
	}

	public StateDefinition getStateDefinition()
	{
		return stateDefinition;
	}

}
