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

package org.nightlabs.jfire.scripting;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

public class ResultClassMismatchException
		extends ScriptException
{
	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;
	private Class expectedResultClass;
	private Class actualResultClass;

	public ResultClassMismatchException(
			ScriptRegistryItemID scriptRegistryItemID,
			Class expectedResultClass,
			Class actualResultClass)
	{
		super("Expected and actual result class do not match (" + scriptRegistryItemID.toString() + ")! Expected: " + expectedResultClass.getName() + " Actual: " + actualResultClass.getName());
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.expectedResultClass = expectedResultClass;
		this.actualResultClass = actualResultClass;
	}

	public Class getActualResultClass()
	{
		return actualResultClass;
	}

	public Class getExpectedResultClass()
	{
		return expectedResultClass;
	}

	public ScriptRegistryItemID getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

}
