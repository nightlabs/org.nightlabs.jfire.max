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

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.controller.JDOObjectChangeEvent;
import org.nightlabs.jfire.jdo.controller.JDOObjectController;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.jdo.controller.JDOObjectChangeEvent"
 *		detachable="true"
 *		table="JFireScripting_ScriptParameterSetChangeEvent"
 *
 * @jdo.inheritance strategy="new-table"
 *
 */
public class ScriptParameterSetChangeEvent extends JDOObjectChangeEvent {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_SET_ADDED = "setAdded";
	public static final String EVENT_TYPE_PARAMETER_ADDED = "parameterAdded";
	public static final String EVENT_TYPE_PARAMETER_CHANGED = "parameterChanged";
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long scriptParameterSetID;
	
	/**
	 * @param controller
	 */
	public ScriptParameterSetChangeEvent(JDOObjectController controller) {
		super(controller);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public ScriptParameterSetChangeEvent() {
		super();
	}

	public void setScriptParameterSetID(long scriptParameterSetID) {
		this.scriptParameterSetID = scriptParameterSetID;
	}
	
	public long getScriptParameterSetID() {
		return scriptParameterSetID;
	}
	
	/**
	 * Add a new ScriptParameterSetChangedEvent to the controller
	 * for the ReportRegistry.
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param eventType The eventType of the event to add.
	 * @param changed The id of the item chaned.
	 * @param relative The id of the item related to the changed one (normally its (new) parent) 
	 */
	public static void addChangeEventToController(
			PersistenceManager pm,
			String eventType,
			ScriptParameterSet changed 
		) 
	{
		JDOObjectController controller = JDOObjectController.getObjectController(
				pm,
				ScriptRegistry.SINGLETON_ID.toString()
			);
		ScriptParameterSetChangeEvent event = new ScriptParameterSetChangeEvent(controller);
		event.setEventType(eventType);
		event.setScriptParameterSetID(changed.getScriptParameterSetID());
		controller.addChangeEvent(event);
	}
	
	
}
