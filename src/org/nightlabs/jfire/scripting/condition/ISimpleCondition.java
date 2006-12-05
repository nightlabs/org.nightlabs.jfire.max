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
 * The Base Interface for expressing simple condition which result is always a boolean
 * 
 * The structure of a simple condition always looks like this
 * [variable] [compareOperator] [value]
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface ISimpleCondition
extends ICondition
{
	/**
	 * returns the name of the variable 
	 * @return the name of the variable
	 */
	String getVariableName();
	
	/**
	 * sets the variableName 
	 * @param variableName the variableName to set
	 */
	void setVariableName(String variableName);
	
	/**
	 * returns the value as string
	 * @return the value as string
	 */
	String getValueAsString();
	
	/**
	 * sets the value as String
	 * @param s the value to set
	 */
	void setValueAsString(String s);
	
	/**
	 * returns the {@link CompareOperator} of the simple condition
	 * @return the {@link CompareOperator} of the simple condition
	 */
	CompareOperator getCompareOperator();
	
	/**
	 * sets the compareOperator for the condition
	 * @param compareOperator the {@link CompareOperator} of the condition to set
	 */
	void setCompareOperator(CompareOperator compareOperator);
}
