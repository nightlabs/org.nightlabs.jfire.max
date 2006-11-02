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
package org.nightlabs.jfire.scripting.editor2d;

import java.util.Map;
import java.util.Set;

import org.nightlabs.editor2d.MultiLayerDrawComponent;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * The interface for the root drawcomponent {@link MultiLayerDrawComponent} which
 * contains {@link ScriptDrawComponent}s and which is responsible for assigning the values
 * of the scripts to the corresponding {@link ScriptDrawComponent}s
 *    
 * @author Daniel.Mazurek <at> NightLabs <dot> de
 *
 */
public interface ScriptMultiLayerDrawComponent 
extends MultiLayerDrawComponent 
{
	public static final String PROP_SCRIPT_VALUES = "Script Values";
	
	/**
	 * returns a {@link Set} of all {@link ScriptRegistryItemID}s which are contained in 
	 * the {@link ScriptMultiLayerDrawComponent}  
	 * @return a Set of all ScriptRegistryItemIDs
	 */
	public Set<ScriptRegistryItemID> getScriptRegistryItemIDs();
	
	/**
	 * assigns all the values of the scripts to the contained {@link ScriptDrawComponent}s
	 * with the corresponding {@link ScriptRegistryItemID}
	 * 
	 * @param scriptValues a {@link Map} which contains all the values of the scripts 
	 * for the corresponding {@link ScriptRegistryItemID}s as key
	 * key: ScriptRegistryItemID
	 * value: value of the script
	 */
	public void assignScriptResults(Map<ScriptRegistryItemID, Object> scriptValues);
}
