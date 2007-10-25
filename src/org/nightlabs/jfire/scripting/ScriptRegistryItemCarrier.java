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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.util.Util;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ScriptRegistryItemCarrier implements Serializable {

	/**
	 * 
	 */
	public ScriptRegistryItemCarrier() {
		super();
		childCarriers = new HashSet<ScriptRegistryItemCarrier>();
	}
	
	
	private ScriptRegistryItemCarrier parentCarrier;
	
	private Set<ScriptRegistryItemCarrier> childCarriers;
	
	private ScriptRegistryItemID registryItemID;

	public ScriptRegistryItemCarrier(
			ScriptRegistryItemCarrier parentCarrier, 
			ScriptRegistryItem item,
			boolean recurse
		) 
	{
		this.parentCarrier = parentCarrier;
		this.registryItemID = ScriptRegistryItemID.create(item.getOrganisationID(), item.getScriptRegistryItemType(), item.getScriptRegistryItemID());
		this.childCarriers = new HashSet<ScriptRegistryItemCarrier>();
		if (recurse && (item instanceof NestableScriptRegistryItem)) {
			for (ScriptRegistryItem childItem : ((NestableScriptRegistryItem)item).getChildren()) {
				addChildCarrier(new ScriptRegistryItemCarrier(this, childItem, recurse));
			}
		}
	}
	
	public ScriptRegistryItemCarrier(
			ScriptRegistryItemCarrier parentCarrier, 
			ScriptRegistryItemID itemID
		) 
	{
		this.parentCarrier = parentCarrier;
		this.registryItemID = itemID;
		this.childCarriers = new HashSet<ScriptRegistryItemCarrier>();
	}
	
	public String getRegistryItemType() {
		return registryItemID.scriptRegistryItemType;
	}
	
	public Set<ScriptRegistryItemCarrier> getChildCarriers() {
		return childCarriers;
	}
	
	public ScriptRegistryItemCarrier getParentCarrier() {
		return parentCarrier;
	}
	
	public void setParentCarrier(ScriptRegistryItemCarrier parentCarrier) {
		this.parentCarrier = parentCarrier;
	}
	
	public void addChildCarrier(ScriptRegistryItemCarrier carrier) {
		childCarriers.add(carrier);
	}
	
	public void removeChildCarrier(ScriptRegistryItemCarrier carrier) {
		childCarriers.remove(carrier);
	}
	
	public ScriptRegistryItemID getRegistryItemID() {
		return registryItemID;
	}
	
	public void setRegistryItemID(ScriptRegistryItemID registryItemID) {
		this.registryItemID = registryItemID;
	}
	
	public void removeChildCarrier(ScriptRegistryItemID itemID) {
		for (Iterator iter = childCarriers.iterator(); iter.hasNext();) {
			ScriptRegistryItemCarrier carrier = (ScriptRegistryItemCarrier) iter.next();
			if (carrier.getRegistryItemID().equals(itemID)) {
				iter.remove();
				break;
			}				
		}
	}
	
	@Override
	public int hashCode() {
		return registryItemID != null ? Util.hashCode(registryItemID.toString()) : 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ScriptRegistryItemCarrier))
			return false;
		ScriptRegistryItemCarrier other = (ScriptRegistryItemCarrier) obj;
		return Util.equals(this.registryItemID, other.registryItemID);
	}

	public Collection<ScriptRegistryItemID> getChildScriptRegistryItemIDs()
	throws ModuleException
	{
		Collection<ScriptRegistryItemID> childIDs = new ArrayList<ScriptRegistryItemID>(getChildCarriers().size());
		for (ScriptRegistryItemCarrier childCarrier : getChildCarriers()) {
			childIDs.add(childCarrier.getRegistryItemID());
		}
		return childIDs;
	}	
}
