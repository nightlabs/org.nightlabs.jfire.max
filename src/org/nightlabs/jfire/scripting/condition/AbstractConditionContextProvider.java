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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractConditionContextProvider 
implements IConditionContextProvider 
{
	public AbstractConditionContextProvider(String conditionContext) {
		this.conditionContext = conditionContext;
	}
	
	private String conditionContext;
	public String getConditionContext() {
		return conditionContext;
	}
	
	private Map<ScriptRegistryItemID, ScriptConditioner> scriptID2ScriptConditioner;
	/**
	 * 
	 * @return
	 */
	protected Map<ScriptRegistryItemID, ScriptConditioner> getScriptID2ScriptConditioner() 
	{
		if (scriptID2ScriptConditioner == null) {
			Set<ScriptRegistryItemID> scriptIDs = getAllowedScriptRegistryItemIDs();
			scriptID2ScriptConditioner = new HashMap<ScriptRegistryItemID, ScriptConditioner>(scriptIDs.size());
			for (ScriptRegistryItemID scriptID : scriptIDs) 
			{
				ScriptConditioner sc = new ScriptConditioner(scriptID, getVariableName(scriptID), 
						getCompareOperators(scriptID), getPossibleValues(scriptID), new LabelProvider());
				scriptID2ScriptConditioner.put(scriptID, sc);
			}
		}
		return scriptID2ScriptConditioner;
	}
	
	private Map<ScriptRegistryItemID, List<CompareOperator>> scriptID2CompareOperators;
	protected Map<ScriptRegistryItemID, List<CompareOperator>> getCompareOperators() {
		if (scriptID2CompareOperators == null) {
			Set<ScriptRegistryItemID> scriptIDs = getAllowedScriptRegistryItemIDs();
			scriptID2CompareOperators = new HashMap<ScriptRegistryItemID, List<CompareOperator>>(scriptIDs.size());
			// TODO: call ScriptManager.getScripts(Collection<ScriptRegistryItemID> scriptItemsIDs)
			// to get all scriptResultClasses and then fill the scriptID2CompareOperators with
			// appropriate values
		}
		return scriptID2CompareOperators;
	}
	
	protected List<CompareOperator> getCompareOperators(ScriptRegistryItemID scriptID) 
	{
//		return getCompareOperators().get(scriptID);
		return getAllOperators();
	}
	
	private List<CompareOperator> allOperators;
	private List<CompareOperator> getAllOperators() 
	{
		if (allOperators == null) {
			allOperators = CollectionUtil.enum2List(Enum.valueOf(CompareOperator.class, "EQUAL"));
//			allOperators = CollectionUtil.enum2List(CompareOperator.EQUAL);
		}
		return allOperators;
	}
	
	public ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptID) 
	{
		if (!getScriptID2ScriptConditioner().containsKey(scriptID))
			throw new IllegalArgumentException("param scriptID "+scriptID+" is not part of the allowed ScriptRegistryIDs");
		
		return getScriptID2ScriptConditioner().get(scriptID);
	}
 	
	public String getVariableName(ScriptRegistryItemID scriptID) 
	{
		if (!getScriptID2VariableName().containsKey(scriptID))
			throw new IllegalArgumentException("param scriptID "+scriptID+" is not part of the allowed ScriptRegistryIDs");
		
		return getScriptID2VariableName().get(scriptID);
	}

	private Map<ScriptRegistryItemID, String> scriptID2VariableName;
	/**
	 * The default Implementation just uses the scriptRegistryItemID of the
	 * {@link ScriptRegistryItemID} as variableName  
	 */
	protected Map<ScriptRegistryItemID, String> getScriptID2VariableName() 
	{
		if (scriptID2VariableName == null) {
			Set<ScriptRegistryItemID> scriptIDs = getAllowedScriptRegistryItemIDs();
			scriptID2VariableName = new HashMap<ScriptRegistryItemID, String>(scriptIDs.size());
			for (ScriptRegistryItemID itemID : scriptIDs) {
				scriptID2VariableName.put(itemID, itemID.scriptRegistryItemID);
			}
		}
		return scriptID2VariableName;
	}
}
