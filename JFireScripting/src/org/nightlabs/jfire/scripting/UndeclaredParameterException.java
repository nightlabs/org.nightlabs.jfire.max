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

import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * <p>
 * A parameter is declared by the script and defined (i.e. parameter value passed) shortly before
 * execution. Hence, it can happen, that a script does not declare (see {@link ScriptRegistryItem#getParameterSet()})
 * a parameter that is passed before execution. In this case, this exception is thrown.
 * </p>
 * <p>
 * In other words: It is thrown by {@link ScriptExecutor#prepare(Script, java.util.Map)}, if not all
 * parameters passed to the {@link ScriptExecutor} are declared by {@link ScriptRegistryItem#getParameterSet()}.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UndeclaredParameterException
		extends ScriptException
{
	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;

	private Set<String> undeclaredParameterIDs;

	public UndeclaredParameterException(
			ScriptRegistryItemID scriptRegistryItemID,
			Set<String> undefinedParameterIDs)
	{
		super("Not all parameters for which values were passed are declared (" + scriptRegistryItemID.toString() + ")! Undeclared: " + undefinedParameterIDs);
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.undeclaredParameterIDs = undefinedParameterIDs;
	}

	public ScriptRegistryItemID getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

	public Set<String> getUndeclaredParameterIDs()
	{
		return undeclaredParameterIDs;
	}
}
