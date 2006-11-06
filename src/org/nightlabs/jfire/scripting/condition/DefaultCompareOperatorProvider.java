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

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DefaultCompareOperatorProvider
implements ICompareOperatorProvider
{
	public List<CompareOperator> getCompareOperator(Class scriptResultClass) 
	{
		if (scriptResultClass.equals(String.class)) {
			return getEqualOperators();			
		}
		if (scriptResultClass.equals(Integer.class)) {
			return getPrimtiveCompareOperators();
		}
		if (scriptResultClass.equals(Float.class)) {
			return getPrimtiveCompareOperators();
		}
		if (scriptResultClass.equals(Double.class)) {
			return getPrimtiveCompareOperators();
		}
		if (scriptResultClass.equals(Short.class)) {
			return getPrimtiveCompareOperators();
		}
		if (scriptResultClass.equals(Long.class)) {
			return getPrimtiveCompareOperators();
		}
//		if (Comparable.class.isAssignableFrom(scriptResultClass))
//			return getAllOperators(); 
		return getEqualOperators();
	}
	
	protected List<CompareOperator> getPrimtiveCompareOperators() {
		return getAllOperators();
	}
	
	private List<CompareOperator> allOperators;
	protected List<CompareOperator> getAllOperators() 
	{
		if (allOperators == null) {
			allOperators = CollectionUtil.enum2List(Enum.valueOf(CompareOperator.class, "EQUAL"));			
		}
		return allOperators;
	}
	
	private List<CompareOperator> equalOperators;
	protected List<CompareOperator> getEqualOperators() 
	{
		if (equalOperators == null) {
			equalOperators = new ArrayList<CompareOperator>(2);
			equalOperators.add(CompareOperator.EQUAL);
			equalOperators.add(CompareOperator.NOT_EQUAL);			
		}		
		return equalOperators;
	}
	
}
