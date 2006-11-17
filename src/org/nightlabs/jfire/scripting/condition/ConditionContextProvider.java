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
package org.nightlabs.jfire.scripting.condition;

import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.condition.id.ConditionContextProviderID"
 *		detachable="true"
 *		table="JFireScripting_ConditionContextProvider"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class field-order="organisationID, conditionContextProviderID"
 *
 */
public class ConditionContextProvider 
//implements IConditionContextProvider 
{
	/** 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */	
//	private String conditionContext;
//	
//	/**
//	 * returns the condition context
//	 * @return the condition context
//	 */
//	public String getConditionContext() {
//		return conditionContext;
//	}
	
	/** 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String conditionContextProviderID;

	/**
	 * 
	 * @return the conditonContextProviderID
	 */
	public String getConditionContextProviderID() {
		return conditionContextProviderID;
	}
	
	/**
	 * TODO: Because your class was incomplete, I added this constructor. Marco.
	 *
	 * @deprecated Only for JDO!
	 */
	protected ConditionContextProvider()
	{
	}

	/**
	 * @param organisationID the organisationID
	 * @param conditionContextProviderID the id for the context
	 */
	public ConditionContextProvider(String organisationID, String conditionContextProviderID)
	{
		this.organisationID = organisationID;
		this.conditionContextProviderID = conditionContextProviderID;
//		this.conditionContext = conditionContext;
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.scripting.id.ScriptRegistryItemID"
	 */	
	private Set scriptRegistryItemIDs;

	/**
	 * returns a {@link Set} of {@link ScriptRegistryItemID}s for the context 
	 * @return a {@link Set} of {@link ScriptRegistryItemID}s for the context 
	 */
	public Set<ScriptRegistryItemID> getScriptRegistryItemIDs() {
		return scriptRegistryItemIDs;
	}

	/**
	 * sets the set of scriptRegistryItemIDs for the context
	 * @param scriptRegistryItemIDs the scriptRegistryItemIDs to set
	 */
	public void setScriptRegistryItemIDs(Set<ScriptRegistryItemID> scriptRegistryItemIDs) {
		this.scriptRegistryItemIDs = scriptRegistryItemIDs;
	}
	
	/**
	 * adds a {@link ScriptRegistryItemID} to the context
	 * @param scriptID the {@link ScriptRegistryItemID} to add
	 */
	public void addScriptRegistryItemID(ScriptRegistryItemID scriptID) {
		scriptRegistryItemIDs.add(scriptID);
	}

	/**
	 * removes a {@link ScriptRegistryItemID} from the context
	 * @param scriptID the {@link ScriptRegistryItemID} to remove
	 */
	public void removeScriptRegistryItemID(ScriptRegistryItemID scriptID) {
		scriptRegistryItemIDs.remove(scriptID);
	}
	
//	public String getVariableName(ScriptRegistryItemID scriptID) {
//		return null;
//	}

}
