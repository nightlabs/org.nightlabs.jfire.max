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
package org.nightlabs.jfire.scripting.editor2d.script;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class Script 
{
	private String language;
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	private String text;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	private Map<ScriptRegistryItemID, String> scriptID2VariableName = new HashMap<ScriptRegistryItemID, String>();
	public Map<ScriptRegistryItemID, String> getScriptID2VariableName() {
		return scriptID2VariableName;		
	}
	
	public void addVariableName(ScriptRegistryItemID scriptID, String value) {
		scriptID2VariableName.put(scriptID, value);
	}
}
