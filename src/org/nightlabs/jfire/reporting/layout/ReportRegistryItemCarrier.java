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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * Carrier organisable in a tree like structure. Used to transfer the 
 * registry structure to the client.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportRegistryItemCarrier implements Serializable {

	/**
	 * 
	 */
	public ReportRegistryItemCarrier() {
		super();
		childCarriers = new HashSet<ReportRegistryItemCarrier>();
	}
	
	
	private ReportRegistryItemCarrier parentCarrier;
	
	private Set<ReportRegistryItemCarrier> childCarriers;
	
	private ReportRegistryItemID registryItemID;
	
	private String registryItemType;

	public ReportRegistryItemCarrier(
			ReportRegistryItemCarrier parentCarrier, 
			ReportRegistryItem item,
			boolean recurse
		) 
	{
		this.parentCarrier = parentCarrier;
		this.registryItemID = ReportRegistryItemID.create(item.getOrganisationID(), item.getReportRegistryItemID());
		this.childCarriers = new HashSet<ReportRegistryItemCarrier>();
		this.registryItemType = item.getReportRegistryItemType();
		if (recurse && (item instanceof NestableReportRegistryItem)) {
			for (ReportRegistryItem childItem : ((NestableReportRegistryItem)item).getChildItems()) {
				addChildCarrier(new ReportRegistryItemCarrier(this, childItem, recurse));
			}
		}
	}
	
	public ReportRegistryItemCarrier(ReportRegistryItemCarrier parentCarrier, String itemType, ReportRegistryItemID itemID) {
		this.parentCarrier = parentCarrier;
		this.registryItemID = itemID;
		this.registryItemType = itemType;
		this.childCarriers = new HashSet<ReportRegistryItemCarrier>();
	}
	
	public String getRegistryItemType() {
		return registryItemType;
	}
	
	public void setRegistryItemType(String registryItemType) {
		this.registryItemType = registryItemType;
	}
	
	public Set<ReportRegistryItemCarrier> getChildCarriers() {
		return childCarriers;
	}
	
	public ReportRegistryItemCarrier getParentCarrier() {
		return parentCarrier;
	}
	
	public void setParentCarrier(ReportRegistryItemCarrier parentCarrier) {
		this.parentCarrier = parentCarrier;
	}
	
	public void addChildCarrier(ReportRegistryItemCarrier carrier) {
		childCarriers.add(carrier);
	}
	
	public void removeChildCarrier(ReportRegistryItemCarrier carrier) {
		childCarriers.remove(carrier);
	}
	
	public ReportRegistryItemID getRegistryItemID() {
		return registryItemID;
	}
	
	public void setRegistryItemID(ReportRegistryItemID registryItemID) {
		this.registryItemID = registryItemID;
	}
	
	public void removeChildCarrier(ReportRegistryItemID itemID) {
		for (Iterator iter = childCarriers.iterator(); iter.hasNext();) {
			ReportRegistryItemCarrier carrier = (ReportRegistryItemCarrier) iter.next();
			if (carrier.getRegistryItemID().equals(itemID)) {
				iter.remove();
				break;
			}				
		}
	}

}
