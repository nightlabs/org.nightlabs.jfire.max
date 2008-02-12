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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.impl.RootDrawComponentImpl;
import org.nightlabs.jfire.scripting.condition.Script;
import org.nightlabs.jfire.scripting.editor2d.ScriptDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.ScriptingConstants;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptRootDrawComponentImpl 
extends RootDrawComponentImpl 
implements ScriptRootDrawComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ScriptRootDrawComponentImpl.class);
	
	public void assignScriptResults(Map<ScriptRegistryItemID, Object> scriptValues) 
	{
		// TODO: all scriptDrawComponents should already be cached when added
		Collection scriptDrawComponents = findDrawComponents(ScriptDrawComponent.class, true);				
		for (Iterator it = scriptDrawComponents.iterator(); it.hasNext(); ) 
		{
			ScriptDrawComponent sd = (ScriptDrawComponent) it.next();
			Object value = scriptValues.get(sd.getScriptRegistryItemID());
			if (sd.getScriptRegistryItemID() == null) {
				logger.error("ScriptDrawComponent "+sd+" has null as scriptRegistryItemID!");
				throw new IllegalStateException("ScriptDrawComponent "+sd+" has null as scriptRegistryItemID!");
			}
			else if (value == null) {
				if (scriptValues.containsKey(sd.getScriptRegistryItemID()))
					logger.error("scriptValues does contain an entry but has a null value for : " + sd.getScriptRegistryItemID());
				else
					logger.error("scriptValues does not contain an entry for: " + sd.getScriptRegistryItemID());

				for (Map.Entry<ScriptRegistryItemID, Object> me : scriptValues.entrySet()) {
					logger.error("  " + me.getKey() + " => " + me.getValue());
				}

				throw new IllegalStateException("scriptValues does not contain an entry (or entry has a null value) for " + sd.getScriptRegistryItemID());
			}
//			if (!(value instanceof String))
//				throw new IllegalStateException("scriptValues does not contain an entry which is NOT a String instance but " + value + " for " + sd.getScriptRegistryItemID());
			sd.setScriptValue(value);
		}		
		firePropertyChange(PROP_SCRIPT_VALUES, null, null);
	}
	
	public Set<ScriptRegistryItemID> getScriptRegistryItemIDs() 
	{
		// TODO: all scriptDrawComponents should already be cached when added
		Collection scriptDrawComponents = findDrawComponents(ScriptDrawComponent.class, true);
		
		Set<ScriptRegistryItemID> scriptIDs = new HashSet<ScriptRegistryItemID>();
		for (Iterator it = scriptDrawComponents.iterator(); it.hasNext(); ) {
			ScriptDrawComponent sd = (ScriptDrawComponent) it.next();
			scriptIDs.add(sd.getScriptRegistryItemID());
		}
		return scriptIDs;
	}

	public void assignVisibleScriptResults(Map<Long, Boolean> scriptValues) 
	{
		if (scriptValues == null)
			throw new IllegalArgumentException("Param scriptValues must NOT be null!");
	
		for (Map.Entry<Long, Boolean> entry : scriptValues.entrySet()) {
			DrawComponent dc = getDrawComponent(entry.getKey());
			if (dc != null) {
				dc.setVisible(entry.getValue());
			}
		}
	}

	public Map<Long, Script> getVisibleScripts() {
		return getVisibleScripts(this);
	}
	
//	private static final Map<Long, Script> EMPTY_MAP = new HashMap<Long, Script>(0);
	protected Map<Long, Script> getVisibleScripts(DrawComponentContainer dcc) 
	{
		// TODO all DrawComponents with a visibleScript should already be cached
		// when visibleScript is assigned
		Map<Long, Script> dcID2VisibleScript = new HashMap<Long, Script>();
		for (DrawComponent dc : dcc.getDrawComponents()) 
		{
			if (dc instanceof DrawComponentContainer) {
				DrawComponentContainer container = (DrawComponentContainer) dc;
				dcID2VisibleScript.putAll(getVisibleScripts(container));
			}
			else {
				Script script = (Script) dc.getProperties().get(ScriptingConstants.PROP_VISIBLE_SCRIPT);
				if (script != null) {
					 dcID2VisibleScript.put(dc.getId(), script);
				}				
			}
		}
		return dcID2VisibleScript;
	}		
}
