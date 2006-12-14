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
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleCondition 
extends AbstractCondition 
implements ISimpleCondition
{
	public SimpleCondition(ScriptRegistryItemID scriptID, CompareOperator compareOperator,
			Object value) 
	{
		if (scriptID == null)
			throw new IllegalArgumentException("Param scriptID must not be null");
		
		if (compareOperator == null)
			throw new IllegalArgumentException("Param compareOperator must not be null");

		if (value == null)
			throw new IllegalArgumentException("Param value must not be null");
		
		this.scriptRegistryItemID = scriptID;
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
	
	private ScriptRegistryItemID scriptRegistryItemID;
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}
	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID) {
		this.scriptRegistryItemID = scriptRegistryItemID;
	}
	
	private Object value;
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
		
//	@Override
//	public boolean equals(Object obj) 
//	{
//		if (obj == null)
//			return false;		
//		if (obj instanceof SimpleCondition) {
//			SimpleCondition sc = (SimpleCondition) obj;			
//			if (!sc.getScriptRegistryItemID().equals(scriptRegistryItemID))
//				return false;
//			if (sc.getCompareOperator() != compareOperator)
//				return false;
//			if (!sc.getValue().equals(value))
//				return false;
//			
//			return true;
//		}		
//		return false;
//	}
//	
//	@Override
//	public int hashCode() 
//	{
//		return scriptRegistryItemID.hashCode() + value.hashCode() + compareOperator.hashCode();
//	}

}
