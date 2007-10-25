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
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.TransitionNameID"
 *		detachable="true"
 *		table="JFireJbpm_TransitionName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="
 *				processDefinitionOrganisationID, processDefinitionID,
 *				stateDefinitionOrganisationID, stateDefinitionID,
 *				transitionOrganisationID, transitionID"
 *
 * @jdo.fetch-group name="Transition.name" fields="transition, names"
 */
public class TransitionName extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String stateDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String transitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String transitionID;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireJbpm_TransitionName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map names = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Transition transition;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TransitionName()
	{
	}

	public TransitionName(Transition transition)
	{
		this.processDefinitionOrganisationID = transition.getProcessDefinitionOrganisationID();
		this.processDefinitionID = transition.getProcessDefinitionID();
		this.stateDefinitionOrganisationID = transition.getStateDefinitionOrganisationID();
		this.stateDefinitionID = transition.getStateDefinitionID();
		this.transitionOrganisationID = transition.getTransitionOrganisationID();
		this.transitionID = transition.getTransitionID();
		this.transition = transition;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return transitionOrganisationID + ':' + transitionID;
	}

	public Transition getTransition()
	{
		return transition;
	}
	public String getProcessDefinitionOrganisationID()
	{
		return processDefinitionOrganisationID;
	}
	public String getProcessDefinitionID()
	{
		return processDefinitionID;
	}
	public String getStateDefinitionOrganisationID()
	{
		return stateDefinitionOrganisationID;
	}
	public String getStateDefinitionID()
	{
		return stateDefinitionID;
	}
	public String getTransitionOrganisationID()
	{
		return transitionOrganisationID;
	}
	public String getTransitionID()
	{
		return transitionID;
	}
}
