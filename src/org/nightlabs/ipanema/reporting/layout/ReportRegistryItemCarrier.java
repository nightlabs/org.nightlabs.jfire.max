/**
 * 
 */
package org.nightlabs.ipanema.reporting.layout;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nightlabs.ipanema.reporting.layout.id.ReportRegistryItemID;

/**
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
