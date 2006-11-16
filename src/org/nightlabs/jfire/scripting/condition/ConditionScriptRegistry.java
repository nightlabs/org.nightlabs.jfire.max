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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;


/**
 * <p>
 * This class is the entry point for the management of {@link IConditionContextProvider}
 * </p>
 * <p>
 * This is a JDO singleton - i.e. one instance per datastore managed by JDO.
 * </p>
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class = "org.nightlabs.jfire.scripting.condition.id.ConditionScriptRegistryID"
 *		detachable="true"
 *		table="JFireScripting_ConditionScriptRegistry"
 *
 * @jdo.create-objectid-class field-order="organisationID, conditionScriptRegistryID"
 * 
 * @jdo.inheritance strategy="new-table"
 */
public class ConditionScriptRegistry 
{
	/**
	 * @jdo.field primary-key="true"
	 */
	private int conditionScriptRegistryID;

	public static ConditionScriptRegistry getConditionScriptRegistry(PersistenceManager pm)
	{
		Iterator it = pm.getExtent(ConditionScriptRegistry.class).iterator();
		if (it.hasNext())
			return (ConditionScriptRegistry) it.next();

		ConditionScriptRegistry reg = new ConditionScriptRegistry(LocalOrganisation.getLocalOrganisation(pm).getOrganisationID(), 0);
		reg = (ConditionScriptRegistry) pm.makePersistent(reg);

		try {
			// TODO: register defaultContextProvider
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return reg;
	}	

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @deprecated for JDO only 
	 */
	protected ConditionScriptRegistry() { }
	
	/**
	 * Don't call this constructor directly. Use {@link #getConditionScriptRegistry(PersistenceManager) } instead!
	 */
	protected ConditionScriptRegistry(String organisationID, int conditionScriptRegistryID) {
		this.organisationID = organisationID;
		this.conditionScriptRegistryID = conditionScriptRegistryID;
	}

	public int getConditionScriptRegistryID() {
		return conditionScriptRegistryID;
	}
	
	/**
	 * key: condition context<br/>
	 * value: {@link ConditionContextProvider}
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.scripting.condition.ConditionContextProvider"
	 *		table="JFireScripting_ConditionScriptRegistry_context2ContextProvider"
	 *		mapped-by="conditionContext"
	 *
	 * @jdo.key mapped-by="conditionContext"
	 */	
//	private Map context2ContextProvider = new HashMap();
	private Map<String, ConditionContextProvider> context2ContextProvider = new HashMap<String, ConditionContextProvider>();
	
	/**
	 * @param contextProvider the {@link ConditionContextProvider} to add
	 */
	public void registerConditionContextProvider(ConditionContextProvider contextProvider) 
	{
		if (contextProvider == null)
			throw new IllegalArgumentException("Param contextProvider must NOT be null!");
		
		context2ContextProvider.put(contextProvider.getConditionContext(), contextProvider);		
	}
	
	/**
	 * @param contextProvider the {@link ConditionContextProvider} to remove
	 */
	public void unregisterConditionContextProvider(ConditionContextProvider contextProvider) 
	{
		if (contextProvider == null)
			throw new IllegalArgumentException("Param contextProvider must NOT be null!");
		
		context2ContextProvider.remove(contextProvider.getConditionContext());		
	}	
	
	/**
	 * 
	 * @param context the context to get the conditionContextProvider for 
	 * @return the {@link ConditionContextProvider} for the given context or null
	 * if no conditionContextProvider was registered for this context
	 */
	public ConditionContextProvider getConditionContextProvider(String context) {
//		return (ConditionContextProvider) context2ContextProvider.get(context);
		return context2ContextProvider.get(context);
	}
	
	/**
	 * key: {@link ScriptRegistryItemID} <br/>
	 * value: {@link IPossibleValueProvider}
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="org.nightlabs.jfire.scripting.id.ScriptRegistryItemID"
	 *		value-type="org.nightlabs.jfire.scripting.condition.IPossibleValueProvider"
	 *		table="JFireScripting_ConditionScriptRegistry_scriptRegistryItemID2PossibleValueProvider"
	 *
	 * 
	 */	
//	private Map scriptRegistryItemID2PossibleValueProvider = new HashMap();
	private Map<ScriptRegistryItemID, IPossibleValueProvider> scriptRegistryItemID2PossibleValueProvider = 
		new HashMap<ScriptRegistryItemID, IPossibleValueProvider>();
	 
}
