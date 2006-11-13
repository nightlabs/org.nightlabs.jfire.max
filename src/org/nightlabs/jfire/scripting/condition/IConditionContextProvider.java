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
import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IConditionContextProvider 
{
	/**
	 * 
	 * @return a {@link Set} of ScriptRegistryItemIDs which are legal in this context
	 */
	public Set<ScriptRegistryItemID> getAllowedScriptRegistryItemIDs();
	
	/**
	 * 
	 * @param scriptID the ScriptRegistryItemID to get the corresponding {@link ScriptConditioner} for 
	 * @return the corresponding {@link ScriptConditioner} for the given scriptID 
	 */
	public ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptID);
			
	/**
	 * 
	 * @return a String which is the context for the provider
	 *  
	 */	
	public String getConditionContext();
	
	/**
	 * 
	 * @param scriptID the ScriptRegistryItemID to get a variableName for
	 * @return the variableName of the given ScriptRegistryItemID
	 */
	public String getVariableName(ScriptRegistryItemID scriptID); 
	
	/**
	 * 
	 * @param scriptID the id of the script to get possible values for
	 * @return a List of Objects which are possible values for the result of script
	 * with the given ScriptRegistryItemID
	 * 
	 */
	public List<Object> getPossibleValues(ScriptRegistryItemID scriptID);
}
