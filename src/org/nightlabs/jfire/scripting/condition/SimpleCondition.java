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


/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleCondition 
extends AbstractCondition 
implements ISimpleCondition
{
	public SimpleCondition(String variableName, CompareOperator compareOperator,
			String value) 
	{
		if (variableName == null)
			throw new IllegalArgumentException("Param variableName must not be null");
		
		if (compareOperator == null)
			throw new IllegalArgumentException("Param compareOperator must not be null");

		if (value == null)
			throw new IllegalArgumentException("Param value must not be null");
		
		this.variableName = variableName;
		this.compareOperator = compareOperator;
		this.value = value;
	}
				
	private CompareOperator compareOperator;
	public CompareOperator getCompareOperator() {
		return compareOperator;
	}
	public void setCompareOperator(CompareOperator compareOperator) {
		this.compareOperator = compareOperator;
	}
	
	private String value = "";
	public String getValueAsString() {
		return value;
	}
	public void setValueAsString(String value) {
		this.value = value;
	}
	
	private String variableName;
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

//	public String getScriptText() 
//	{
//		IConditionGenerator generator = GeneratorRegistry.sharedInstance().getGenerator(getLanguage());
//		String openContainer = generator.getOpenContainerString();
//		String variable = generator.getVariableString() + getVariableName();
//		String operator = generator.getCompareOperator(getCompareOperator());
//		String closeContainer = generator.getCloseContainerString();
//		return openContainer + variable + operator + getValueAsString() + closeContainer;
//	}

}
