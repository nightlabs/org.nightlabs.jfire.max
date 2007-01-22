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

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaScript;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class JavaScriptConditionGenerator  
extends ConstrainedConditionGenerator
{
//	public JavaScriptConditionGenerator(Collection<ScriptConditioner> scriptConditioners) {
//		super(scriptConditioners);
//	}

	public JavaScriptConditionGenerator() {
		super();
	}

	@Override
	public String getCombineOperator(CombineOperator combineOperator) 
	{
		switch (combineOperator) 
		{
			case LOGICAL_AND:
				return "&&";
			case LOGICAL_OR:
				return "||";
//			case LOGICAL_NOT:
//				return "!";				
		}
		return null;
	}

	@Override
	public String getCompareOperator(CompareOperator compareOperator) 
	{
		switch (compareOperator) 
		{
			case EQUAL:
				return "==";
			case NOT_EQUAL:
				return "!=";
			case GREATER_THEN:
				return ">";
			case SMALLER_THEN:
				return "<";
			case GREATER_OR_EQUAL_THEN:
				return ">=";
			case SMALLER_OR_EQUAL_THEN:
				return "<=";				
		}
		return null;
	}

	@Override
	public String getVariableString() {
//		return "$";
		return "";
	}

	@Override
	public String getCloseContainerString() {
		return ")";
	}
	
	@Override
	public String getOpenContainerString() {
		return "(";
	}

//	private List<String> compareOperatorStrings = null;
//	public List<String> getCompareOperators() {
//		if (compareOperatorStrings == null) {
//			CompareOperator[] compareOperators = CompareOperator.values();
//			compareOperatorStrings = new ArrayList<String>(compareOperators.length);
//			for (int i=0; i<compareOperators.length; i++) {
//				compareOperatorStrings.add(getCompareOperator(compareOperators[i]));
//			}			
//		}
//		return compareOperatorStrings;
//	}
	
//	private List<String> combineOperatorStrings = null;
//	public List<String> getCombineOperators() 
//	{
//		if (combineOperatorStrings == null) {
//			CombineOperator[] combineOperators = CombineOperator.values();
//			combineOperatorStrings = new ArrayList<String>(combineOperators.length);
//			for (int i=0; i<combineOperators.length; i++) {
//				combineOperatorStrings.add(getCombineOperator(combineOperators[i]));
//			}			
//		}
//		return combineOperatorStrings;		
//	}

	@Override
	public CompareOperator getCompareOperator(String compareOperator) 
	{		
		if (compareOperator.equals("==")) {
			return CompareOperator.EQUAL;
		}
		else if (compareOperator.equals("!=")) {
			return CompareOperator.NOT_EQUAL;
		}
		else if (compareOperator.equals(">")) {
			return CompareOperator.GREATER_THEN;
		}
		else if (compareOperator.equals("<")) {
			return CompareOperator.SMALLER_THEN;
		}
		else if (compareOperator.equals(">=")) {
			return CompareOperator.GREATER_OR_EQUAL_THEN;
		}
		else if (compareOperator.equals("<=")) {
			return CompareOperator.SMALLER_OR_EQUAL_THEN;
		}
		else
			throw new IllegalArgumentException("Param compareOperator ("+compareOperator+") is not a valid compareOperator String");
	}

	@Override
	public CombineOperator getCombineOperator(String combineOperator) 
	{
		if (combineOperator.equals("&&")) {
			return CombineOperator.LOGICAL_AND;
		}
		else if (combineOperator.equals("||")) {
			return CombineOperator.LOGICAL_OR;
		}
//		else if (combineOperator.equals("!")) {
//			return CombineOperator.LOGICAL_NOT;
//		}
		else
			throw new IllegalArgumentException("Param combineOperator ("+combineOperator+") is not a valid combineOperator String");
	}

	@Override
	public String getLanguage() {
		return ScriptExecutorJavaScript.LANGUAGE_JAVA_SCRIPT;
	}	
	
	@Override
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
			String varName = getVariableString() + getScriptID2Name().get(simpleCondition.getScriptRegistryItemID());
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
}
