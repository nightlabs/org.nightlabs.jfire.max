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

import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * This is the base interface for all {@link DrawComponent}s which should obtain
 * information from a script whose ID is a {@link ScriptRegistryItemID}
 * 
 * 
 * @author Daniel.Mazurek <at> NightLabs <dot> de
 *
 */
public interface ScriptDrawComponent extends DrawComponent
{
	public static final String PROP_SCRIPT_REGISTRY_ITEM_ID = "ScriptRegistryItemID";
	
	/**
	 * sets the {@link ScriptRegistryItemID} of the script
	 * @param scriptRegistryItemID the ID of the script
	 */
	void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID);
	
	/**
	 * returns the {@link ScriptRegistryItemID} of the script
	 * @return the ID of the script
	 */
	ScriptRegistryItemID getScriptRegistryItemID();
	
//	void setText(String text);
//	String getText();

	/**
	 * sets the value of the script
	 * @param scriptValue the value of the script to set
	 */
	void setScriptValue(Object scriptValue);
	
	/**
	 * returns the value of the script
	 * @return the value of the script
	 */
	Object getScriptValue();
}