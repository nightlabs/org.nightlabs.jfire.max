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
	 * @param text the scriptText to transform into a ICondition
	 * @param simpleStringConditions determines if the ICondition contains {@link ISimpleCondition}s or
	 * {@link ISimpleStringCondition}s
	 * 
	 * @return the ICondition which has been parsed from the given scriptText
	 */
	ICondition getCondition(String text, boolean simpleStringConditions);
	
	/**
	 * 
	 * @param condition the {@link ICondition} to transform into language dependend string
	 * @return the language dependend string of the fiven condition
	 */
	String getScriptText(ICondition condition);
	
	/**
	 * 
	 * @return the script language as String
	 */
	String getLanguage();
	
	/**
	 * 
	 * @return a Collection of {@link ScriptConditioner} which provide the necessary data
	 * for parsing and creating the scriptTexts
	 */
	Collection<ScriptConditioner> getScriptConditioner();
	
	/**
	 * set the Collection of {@link ScriptConditioner} which provide the necessary data
	 * for parsing and creating the scriptTexts
	 * 
	 * @param scriptConditioner the scriptConditioner to set
	 */
	void setScriptConditioner(Collection<ScriptConditioner> scriptConditioner);
}
