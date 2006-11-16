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

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * The Base Interface for expressing simple condition which result is always a boolean
 * 
 * The structure of a simple condition always looks like this
 * [variable] [compareOperator] [value]
 * where the variable is expressed as a ScriptRegistryItemID
 * the compareOperator is a enum which describes the comapre operation
 * and the value must also be compatible to the type of the variable
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface ISimpleCondition
extends ICondition
{
	/**
	 * 
	 * @param scriptID the ScriptRegistryItemID which reponds to the variable 
	 * of the simple condition
	 * 
	 */
	void setScriptRegistryItemID(ScriptRegistryItemID scriptID);
	
	/**
	 * 
	 * @return the ScriptRegistryItemID which reponds to the variable 
	 * of the simple condition
	 */
	ScriptRegistryItemID getScriptRegistryItemID();

	/**
	 * 
	 * @return the value as string
	 */
	String getValueAsString();
	
	/**
	 * 
	 * @param value the value object
	 */
	void setValue(Object value);
	
	/**
	 * 
	 * @return the {@link CompareOperator} of the simple condition
	 */
	CompareOperator getCompareOperator();
	
	/**
	 * 
	 * @param compareOperator the {@link CompareOperator} of the condition to set
	 */
	void setCompareOperator(CompareOperator compareOperator);
		
//	/**
//	 * 
//	 * @param scriptID the {@link ScriptRegistryItemID} to get a
//	 * {@link ScriptConditioner} for 
//	 * @return the scriptConditioner for the given ScriptRegistryItemID
//	 */
//	ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptID);
	
//	/**
//	 * @return the {@link ScriptConditioner} for the {@link ScriptRegistryItemID} of the
//	 * condition
//	 */
//	ScriptConditioner getScriptConditioner();	
}
