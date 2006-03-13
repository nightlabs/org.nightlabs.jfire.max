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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.controller.JDOObjectChangeEvent;
import org.nightlabs.jfire.jdo.controller.JDOObjectController;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.jdo.controller.JDOObjectChangeEvent"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistryItemChangeEvent"
 *
 * @jdo.inheritance strategy="new-table"
 *
 */
public class ScriptRegistryItemChangeEvent extends JDOObjectChangeEvent {

	public static final String EVENT_TYPE_ITEM_ADDED = "itemAdded";
	public static final String EVENT_TYPE_ITEM_MOVED = "itemMoved";
	public static final String EVENT_TYPE_ITEM_DELETED = "itemDeleted";
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String itemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String relatedItemID;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient ScriptRegistryItemCarrier itemCarrier;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient ScriptRegistryItemCarrier relatedItemCarrier;
	
	/**
	 * @param controller
	 */
	public ScriptRegistryItemChangeEvent(JDOObjectController controller) {
		super(controller);
	}

	/**
	 * @deprecated
	 */
	public ScriptRegistryItemChangeEvent() {
		super();
	}

	/**
	 * @return Returns the itemCarrier.
	 */
	public ScriptRegistryItemCarrier getItemCarrier() {
		if (itemCarrier == null) {
			if (itemID != null) {
				try {
					itemCarrier = new ScriptRegistryItemCarrier(
							null, 
							new ScriptRegistryItemID(itemID)
						);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
			
		return itemCarrier;
	}

	/**
	 * @param itemCarrier The itemCarrier to set.
	 */
	public void setItemCarrier(ScriptRegistryItemCarrier itemCarrier) {
		this.itemCarrier = itemCarrier;
	}

	/**
	 * @return Returns the relatedItemCarrier.
	 */
	public ScriptRegistryItemCarrier getRelatedItemCarrier() {
		if (relatedItemCarrier == null) {
			if (relatedItemID != null) {
				try {
					relatedItemCarrier = new ScriptRegistryItemCarrier(
							null, 
							new ScriptRegistryItemID(relatedItemID)
						);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
			
		return relatedItemCarrier;
	}

	/**
	 * @param relatedItemCarrier The relatedItemCarrier to set.
	 */
	public void setRelatedItemCarrier(ScriptRegistryItemCarrier relatedItemCarrier) {
		this.relatedItemCarrier = relatedItemCarrier;
	}
	
	/**
	 * @return Returns the itemID.
	 */
	public String getItemID() {
		return itemID;
	}

	/**
	 * @param itemID The itemID to set.
	 */
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	/**
	 * @return Returns the relatedItemID.
	 */
	public String getRelatedItemID() {
		return relatedItemID;
	}

	/**
	 * @param relatedItemID The relatedItemID to set.
	 */
	public void setRelatedItemID(String relatedItemID) {
		this.relatedItemID = relatedItemID;
	}

	/**
	 * Add a new ScriptRegistryItemChangedEvent to the controller
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
			ScriptRegistryItem changed, 
			ScriptRegistryItem relative
		) 
	{
		JDOObjectController controller = JDOObjectController.getObjectController(
				pm,
				ScriptRegistry.SINGLETON_ID.toString()
			);
		ScriptRegistryItemChangeEvent event = new ScriptRegistryItemChangeEvent(controller);
		event.setEventType(eventType);
		event.setItemID(JDOHelper.getObjectId(changed).toString());
		
		if (relative != null) {
			event.setRelatedItemID(JDOHelper.getObjectId(relative).toString());
		}
		
		controller.addChangeEvent(event);
	}
	
	
}
