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
 * This Interface generates the language dependend syntax for the
 * operations described in {@link CombineOperator} and {@link CompareOperator}
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IConditionGenerator 
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
	String getCombineOperator(CombineOperator combineOperator);
	
	/**
	 * returns the scriptLanguage depended String for all available
	 * {@link CompareOperator}s like EQUALS, NOT_EQUALS ....
	 * 
	 * e.g. for JavaScript CompareOperator.EQUALS would return "=="
	 * 
	 * @param compareOperator the {@link CompareOperator} to express
	 * @return a String which expresses the CompareOperator as scriptLanguage depended String
	 */
	String getCompareOperator(CompareOperator compareOperator);
	
	/**
	 * returns the scriptLanguage depended String Identifier of a variable
	 *  
	 * @return the scriptLanguage depended String Identifier of a variable 
	 */
	String getVariableString();
	
	/**
	 * 
	 * @return the String which represents the opening of a condition container, 
	 * repectivly in many languages this represents a certain type of bracket
	 */
	String getOpenContainerString();

	/**
	 * 
	 * @return the String which represents the closing of a condition container, 
	 * repectivly in many languages this represents a certain type of bracket
	 */
	String getCloseContainerString();

	/**
	 * 
	 * @return all {@link CompareOperator}s as List of Language dependend Strings
	 */
	List<String> getCompareOperators();

	/**
	 * 
	 * @return all {@link CombineOperator}s as List of Language dependend Strings
	 */
	List<String> getCombineOperators();
	
	/**
	 * 
	 * @param compareOperator the compareOperator as language dependend string
	 * @return the {@link CompareOperator} for the given language dependend string
	 */
	CompareOperator getCompareOperator(String compareOperator);
	
	/**
	 * 
	 * @param combineOperator the combineOperator as language dependend string
	 * @return the {@link CombineOperator} for the given language dependend string
	 */
	CombineOperator getCombineOperator(String combineOperator);
	
///**
// * 
// * @param scriptText the scripText to parse
// * @return the corresponding {@link ICondition} for the scriptText
// */
//public ICondition getCondition(String scriptText);

}
