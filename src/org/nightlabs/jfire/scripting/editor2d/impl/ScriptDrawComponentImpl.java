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
package org.nightlabs.jfire.scripting.editor2d.impl;

import org.nightlabs.editor2d.impl.DrawComponentImpl;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.scripting.editor2d.ScriptDrawComponent;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class ScriptDrawComponentImpl 
extends DrawComponentImpl 
implements ScriptDrawComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient ScriptRegistryItemID scriptRegistryItemID = null;
	private String scriptRegistryItemIDKeyStr = null;

	public ScriptRegistryItemID getScriptRegistryItemID() {
		if (scriptRegistryItemID == null && scriptRegistryItemIDKeyStr != null)
			scriptRegistryItemID = (ScriptRegistryItemID) ObjectIDUtil.createObjectID(scriptRegistryItemIDKeyStr);
		return scriptRegistryItemID;
	}
	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID) 
	{
		ScriptRegistryItemID oldID = this.scriptRegistryItemID; 
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.scriptRegistryItemIDKeyStr = scriptRegistryItemID == null ? null : scriptRegistryItemID.toString();		
		firePropertyChange(PROP_SCRIPT_REGISTRY_ITEM_ID, oldID, scriptRegistryItemID);
	}		
	
	private transient Object scriptValue;
	public Object getScriptValue() {
		return scriptValue;
	}
	public void setScriptValue(Object scriptValue) {
		this.scriptValue = scriptValue;
	}
	
}
