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
import javax.jdo.annotations.Value;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionDescriptionID;

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
@PersistenceCapable(
	objectIdClass=StateDefinitionDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireJbpm_StateDefinitionDescription")
@FetchGroups(
	@FetchGroup(
		name="StateDefinition.description",
		members={@Persistent(name="stateDefinition"), @Persistent(name="descriptions")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StateDefinitionDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 *
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@PrimaryKey
	@Column(length=100)
	@SuppressWarnings("unused")
	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 *
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@PrimaryKey
	@Column(length=50)
	@SuppressWarnings("unused")
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 *
	 * FIXME Check the use of this field and remove it if not used or indented to be used.
	 */
	@PrimaryKey
	@Column(length=100)
	@SuppressWarnings("unused")
	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
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
	 * @jdo.value-column sql-type="CLOB"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireJbpm_StateDefinitionDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	@Value(
			columns={@Column(sqlType="CLOB")}
	)
	private Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
