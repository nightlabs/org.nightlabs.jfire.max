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
public class ConditionUtil
{
	/**
	 * adds all contained {@link ISimpleCondition}s of a condition to the given list
	 * @param condition the condition to get all contained {@link ISimpleCondition}s for
	 * @param simpleConditions a {@link List} to add all contained {@link ISimpleCondition}s to
	 */
	public static void getSimpleConditions(ICondition condition, List<ISimpleCondition> simpleConditions)
	{
		if (condition == null)
			throw new IllegalArgumentException("Param condition must NOT be null!");
		
		if (simpleConditions == null)
			throw new IllegalArgumentException("Param simpleCOnditions must NOT be null!");
		
		if (condition instanceof IConditionContainer) {
			IConditionContainer container = (IConditionContainer) condition;
			for (ICondition condition2 : container.getConditions()) {
				getSimpleConditions(condition2, simpleConditions);
			}
		}
		else if (condition instanceof ISimpleCondition) {
			simpleConditions.add((ISimpleCondition)condition);
		}
	}

	/**
	 * adds all contained {@link ISimpleStringCondition}s of a condition to the given list
	 * @param condition the condition to get all contained {@link ISimpleStringCondition}s for
	 * @param simpleConditions a {@link List} to add all contained {@link ISimpleStringCondition}s to
	 */
	public static void getSimpleStringConditions(ICondition condition, List<ISimpleStringCondition> simpleConditions)
	{
		if (condition == null)
			throw new IllegalArgumentException("Param condition must NOT be null!");
		
		if (simpleConditions == null)
			throw new IllegalArgumentException("Param simpleCOnditions must NOT be null!");
		
		if (condition instanceof IConditionContainer) {
			IConditionContainer container = (IConditionContainer) condition;
			for (ICondition condition2 : container.getConditions()) {
				getSimpleStringConditions(condition2, simpleConditions);
			}
		}
		else if (condition instanceof ISimpleStringCondition) {
			simpleConditions.add((ISimpleStringCondition)condition);
		}
	}
}
