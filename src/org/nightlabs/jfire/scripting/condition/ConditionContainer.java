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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ConditionContainer 
extends AbstractCondition
implements IConditionContainer
{

	private List<ICondition> conditions = new LinkedList<ICondition>();
	public void addCondition(ICondition condition) {
		conditions.add(condition);
	}
	public void removeCondition(ICondition condition) {
		conditions.remove(condition);		
	}

	private CombineOperator combineOperator = CombineOperator.LOGICAL_AND;
	public CombineOperator getCombineOperator() {
		return combineOperator;
	}
	public void setCombineOperator(CombineOperator combineOperator) {
		this.combineOperator = combineOperator;
	}

	public String getScriptText() 
	{
		StringBuffer sb = new StringBuffer();
		for (ICondition condition : conditions) 
		{
			if (condition instanceof ISimpleCondition) {
				ISimpleCondition con = (ISimpleCondition) condition;
				sb.append(con.getScriptText());
			} 
			else if (condition instanceof IConditionContainer) 
			{
				ISyntaxGenerator generator = GeneratorRegistry.sharedInstance().getGenerator(getLanguage());
				IConditionContainer container = (IConditionContainer) condition;
				sb.append(generator.getOpenContainerString());
				sb.append(container.getScriptText());
				sb.append(generator.getCloseContainerString());				
			}
		}
		return sb.toString();
	}

}
