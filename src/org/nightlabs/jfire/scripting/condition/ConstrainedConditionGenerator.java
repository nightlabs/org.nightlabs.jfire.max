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

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class ConstrainedConditionGenerator 
implements IConditionGenerator 
{
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
	
	public ICondition getCondition(String text) {
		return ConstrainedConditionScriptParser.sharedInstance().getCondition(this, text);
	}
	
	public String getScriptText(ICondition condition) 
	{
//		return condition.getScriptText();
		if (condition instanceof ISimpleCondition) {
			ISimpleCondition simpleCondition = (ISimpleCondition) condition;
			String openContainer = getOpenContainerString();
			String variable = getVariableString() + simpleCondition.getVariableName();
			String operator = getCompareOperator(simpleCondition.getCompareOperator());
			String closeContainer = getCloseContainerString();
			return openContainer + variable + operator + simpleCondition.getValueAsString() + closeContainer;			
		}
		if (condition instanceof IConditionContainer) {
			IConditionContainer container = (IConditionContainer) condition;
			StringBuffer sb = new StringBuffer();
			sb.append(getOpenContainerString());
			List<ICondition> conditions = container.getConditions();
			for (int i=0; i<conditions.size(); i++) {
				ICondition con = conditions.get(i);
				sb.append(getScriptText(con));
				if (i != conditions.size()-1)
					sb.append(getCombineOperator(container.getCombineOperator()));
			}
			sb.append(getCloseContainerString());
			return sb.toString();
		}
		else
			return null;
	}
}
