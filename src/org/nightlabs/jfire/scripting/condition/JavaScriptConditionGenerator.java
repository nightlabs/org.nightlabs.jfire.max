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
public class JavaScriptConditionGenerator 
implements IConditionGenerator 
{

	public String getCombineOperator(CombineOperator combineOperator) 
	{
		switch (combineOperator) 
		{
			case LOGICAL_AND:
				return "&&";
			case LOGICAL_OR:
				return "||";
			case LOGICAL_NOT:
				return "!";				
		}
		return null;
	}

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

	public String getVariableString(String variableName) {
		return "$"+variableName;
	}

	public String getCloseContainerString() {
		return ")";
	}

	public String getOpenContainerString() {
		return "(";
	}

	/**
	 * 
	 * @param scriptText the scripText to parse
	 * @return the corresponding {@link ICondition} for the scriptText
	 */
	public ICondition getCondition(String scriptText) {
		// TODO parse scriptText and create appropriate ICondition
		return null;
	}
	 	
}
