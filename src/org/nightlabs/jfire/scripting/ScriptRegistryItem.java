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

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptRegistryItemID"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistryItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemID"
 * 
 * @jdo.fetch-group name="ScriptRegistryItem.parent" fetch-groups="default" fields="parent"
 * @jdo.fetch-group name="ScriptRegistryItem.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ScriptRegistryItem.this" fetch-groups="default" fields="parent, name"
 * 
 * @jdo.query
 *		name="getTopLevelScriptRegistryItems"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.parent == null    
 *			PARAMETERS String paramOrganisationID
 *			IMPORTS import java.lang.String"
 * 
 */
public class ScriptRegistryItem
		implements Serializable, StoreCallback
{
	private static final long serialVersionUID = 9221181132208442543L;
	
	public static final String QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS = "getTopLevelScriptRegistryItems";

	public static final String FETCH_GROUP_PARENT = "ScriptRegistryItem.parentItem";
	public static final String FETCH_GROUP_NAME = "ScriptRegistryItem.name";
	public static final String FETCH_GROUP_THIS_SCRIPT_REGISTRY_ITEM = "ScriptRegistryItem.this";
	
	
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
	
	// TODO: @Marco: Removed item nature as its only two values would be category and script. 
	// This is aleady coverd by the two subclasses. Revision with nature was 3025
	
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
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptRegistryItemName name;

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

	public ScriptRegistryItem(String organisationID, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		this.organisationID = organisationID;
		this.scriptRegistryItemType = scriptRegistryItemType;
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.name = new ScriptRegistryItemName(this);
	}

	public static String getPrimaryKey(String organisationID, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		return organisationID + '/' + scriptRegistryItemType + '/' + scriptRegistryItemID;
	}

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
	
	public ScriptRegistryItemName getName() {
		return name;
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
				Utils.equals(this.scriptRegistryItemID,     other.scriptRegistryItemID);
	}

	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(organisationID) ^
				Utils.hashCode(scriptRegistryItemType) ^
				Utils.hashCode(scriptRegistryItemID);
	}

	/**
	 * Returns all top level (parent == null) ScriptRegistryItems for the given organisationID
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param organisatinID The organisationID to use
	 */
	public static Collection getTopScriptRegistryItems(PersistenceManager pm, String organisatinID) {
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS);
		return (Collection)q.execute(organisatinID);
	}

	public void jdoPreStore() {
		// Assuming that preStore only called when first made persistent
		if (!JDOHelper.isNew(this)) 
			return;
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");
		
		
		ScriptRegistryItem _parent = getParent();
		if (getParameterSet() == null && parent != null) {
			if (JDOHelper.isDetached(parent)) {
				ScriptRegistryItem pParent = null;
				boolean setFromPersistent = false;
				try {
					pParent = (ScriptRegistryItem) pm.getObjectById(JDOHelper.getObjectId(parent));
					setFromPersistent = true;
				} catch(JDOObjectNotFoundException e) {}
				if (setFromPersistent) {
					setParameterSet(pParent.getParameterSet());
				}
				else {
					try {
						this.setParameterSet(_parent.getParameterSet());
					} catch (JDODetachedFieldAccessException e) {
						// TODO: Log with logger?? when made transient -> Nullpointerexception
						System.out.println("WARNING: Could not set the parameterSet initially from null to the parents one");
					}
				}
			}
		}
		
		ScriptRegistryItemChangeEvent.addChangeEventToController(
				pm,
				ScriptRegistryItemChangeEvent.EVENT_TYPE_ITEM_ADDED,
				this,
				getParent()
			);		
	}
	
}
