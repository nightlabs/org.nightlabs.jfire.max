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
 * This Interface describes a container for holding {@link Condition}s
 * 
 * each IConditionContainer has exactly one {@link CombineOperator} which 
 * expresses how the conditions are combined with each other
 * 
 * e.g. if the CombineOperator is {@link CombineOperator#LOGICAL_AND}   
 * 
 * Condition1 LOGICAL_AND Condition2 LOGICAL_AND Condition3 LOGICAL_AND ....
 * 
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IConditionContainer
extends ICondition
{
	void addCondition(ICondition condition);
	void removeCondition(ICondition condition);
	
	void setCombineOperator(CombineOperator combineOperator);
	CombineOperator getCombineOperator();
}
