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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class ConstrainedConditionGenerator 
implements IConditionGenerator 
{
//	public ConstrainedConditionGenerator(Collection<ScriptConditioner> scriptConditioners) 
//	{
//		setScriptConditioner(scriptConditioners);
//	}

	public ConstrainedConditionGenerator() 
	{

	}

	private Collection<ScriptConditioner> scriptConditioners;
	private Map<ScriptRegistryItemID, String> scriptID2Name;	
	private ConstrainedConditionScriptParser parser;
	
	public Collection<ScriptConditioner> getScriptConditioner() {
		return scriptConditioners;
	}
	public void setScriptConditioner(Collection<ScriptConditioner> scriptConditioner) {
		this.scriptConditioners = scriptConditioner;
		int size = scriptConditioners.size();
		scriptID2Name = new HashMap<ScriptRegistryItemID, String>(size);
		for (ScriptConditioner conditioner : scriptConditioners) {
			scriptID2Name.put(conditioner.getScriptRegistryItemID(), conditioner.getVariableName());
		}
	}
	
	public ICondition getCondition(String text) 
	{
		if (parser == null) {
			parser = new ConstrainedConditionScriptParser(scriptConditioners);  
		}		
		return parser.getCondition(this, text);
	}
	
//	public String getScriptText(ICondition condition) 
//	{
//		if (condition instanceof ISimpleCondition) 
//		{			
//			ISimpleCondition simpleCondition = (ISimpleCondition) condition;
//			String openContainer = getOpenContainerString();
//			String closeContainer = getCloseContainerString();
//			String operator = getCompareOperator(simpleCondition.getCompareOperator());
//			String varName = getVariableString() + scriptID2Name.get(simpleCondition.getScriptRegistryItemID());
//			Object value = simpleCondition.getValue();
//			String valueName = "";
//			String variableName = "";
//			if (simpleCondition.getValue() instanceof PersistenceCapable) 
//			{
//				StringBuffer sb = new StringBuffer();
//				sb.append("importPackage(Packages.javax.jdo);");
////				sb.append("importPackage(Packages.org.nightlabs.jdo);");
//				sb.append("JDOHelper.getObjectId("+varName+").toString()");
//				variableName = sb.toString();
//				ObjectID objectID = (ObjectID) JDOHelper.getObjectId(value);
//				String objectIDString = objectID.toString();				
////				valueName = "ObjectIDUtil.createObjectID(\""+objectIDString+"\")";
//				valueName = "\""+objectIDString+"\"";
//			}
//			else {
//				variableName = varName;
//				// TODO: allow only primitive types
//				valueName = String.valueOf(value);
//			}
//			return openContainer + variableName + operator + valueName + closeContainer;
//		}
//		if (condition instanceof IConditionContainer) {
//			IConditionContainer container = (IConditionContainer) condition;
//			StringBuffer sb = new StringBuffer();
//			sb.append(getOpenContainerString());
//			List<ICondition> conditions = container.getConditions();
//			for (int i=0; i<conditions.size(); i++) {
//				ICondition con = conditions.get(i);
//				sb.append(getScriptText(con));
//				if (i != conditions.size()-1)
//					sb.append(getCombineOperator(container.getCombineOperator()));
//			}
//			sb.append(getCloseContainerString());
//			return sb.toString();
//		}
//		else
//			throw new RuntimeException("unknown implementation of ICondition "+condition);
//	}

	public String getScriptText(ICondition condition) 
	{
		StringBuffer sb = new StringBuffer();
		getScriptText(condition, sb, false);
		return sb.toString();
	}
	
	protected void getScriptText(ICondition condition, StringBuffer sb, boolean importInserted) 
	{
		if (condition instanceof ISimpleCondition) 
		{	
			ISimpleCondition simpleCondition = (ISimpleCondition) condition;
			if (simpleCondition.getValue() instanceof PersistenceCapable) {
				if (!importInserted) {
					sb.insert(0, "importPackage(Packages.javax.jdo);");
					importInserted = true;
				}
			}
			String openContainer = getOpenContainerString();
			String closeContainer = getCloseContainerString();
			String operator = getCompareOperator(simpleCondition.getCompareOperator());
			String varName = getVariableString() + scriptID2Name.get(simpleCondition.getScriptRegistryItemID());
			Object value = simpleCondition.getValue();
			String valueName = "";
			String variableName = "";
			if (value instanceof PersistenceCapable) 
			{
				variableName = "JDOHelper.getObjectId("+varName+").toString()";
				ObjectID objectID = (ObjectID) JDOHelper.getObjectId(value);
				String objectIDString = String.valueOf(objectID);
				valueName = "\""+objectIDString+"\"";
			}
			else {
				variableName = varName;
				// TODO: allow only primitive types
				valueName = String.valueOf(value);
			}
			sb.append(openContainer + variableName + operator + valueName + closeContainer);
		}
		else if (condition instanceof IConditionContainer) 
		{
			IConditionContainer container = (IConditionContainer) condition;
			sb.append(getOpenContainerString());
			List<ICondition> conditions = container.getConditions();
			for (int i=0; i<conditions.size(); i++) {
				ICondition con = conditions.get(i);
				if (!importInserted) {
					if (con instanceof ISimpleCondition) {
						if (((ISimpleCondition)con).getValue() instanceof PersistenceCapable) {
							sb.insert(0, "importPackage(Packages.javax.jdo);");
							importInserted = true;
						}									
					}					
				}
				getScriptText(con, sb, importInserted);
				if (i != conditions.size()-1)
					sb.append(getCombineOperator(container.getCombineOperator()));
			}
			sb.append(getCloseContainerString());
		}
		else
			throw new RuntimeException("unknown implementation of ICondition "+condition);
	}
		
	/**
	* returns the scriptLanguage depended String for all available
	* {@link CombineOperator}s like AND, OR ....
	* 
	* e.g. for JavaScript CombineOperator.AND would return "&"
	* 
	* @param combineOperator the {@link CombineOperator} to express
	* @return a String which expresses the CombineOperator as scriptLanguage depended String
	*/
	public abstract String getCombineOperator(CombineOperator combineOperator);
	
	/**
	* returns the scriptLanguage depended String for all available
	* {@link CompareOperator}s like EQUALS, NOT_EQUALS ....
	* 
	* e.g. for JavaScript CompareOperator.EQUALS would return "=="
	* 
	* @param compareOperator the {@link CompareOperator} to express
	* @return a String which expresses the CompareOperator as scriptLanguage depended String
	*/
	public abstract String getCompareOperator(CompareOperator compareOperator);
	
	/**
	* returns the scriptLanguage depended String Identifier of a variable
	*  
	* @return the scriptLanguage depended String Identifier of a variable 
	*/
	public abstract String getVariableString();
	
	/**
	* 
	* @return the String which represents the opening of a condition container, 
	* repectivly in many languages this represents a certain type of bracket
	*/
	public abstract String getOpenContainerString();
	
	/**
	* 
	* @return the String which represents the closing of a condition container, 
	* repectivly in many languages this represents a certain type of bracket
	*/
	public abstract String getCloseContainerString();
	
	/**
	* 
	* @return all {@link CompareOperator}s as List of Language dependend Strings
	*/
	public abstract List<String> getCompareOperators();
	
	/**
	* 
	* @return all {@link CombineOperator}s as List of Language dependend Strings
	*/
	public abstract List<String> getCombineOperators();
	
	/**
	* 
	* @param compareOperator the compareOperator as language dependend string
	* @return the {@link CompareOperator} for the given language dependend string
	*/
	public abstract CompareOperator getCompareOperator(String compareOperator);
	
	/**
	* 
	* @param combineOperator the combineOperator as language dependend string
	* @return the {@link CombineOperator} for the given language dependend string
	*/
	public abstract CombineOperator getCombineOperator(String combineOperator);
	
}
