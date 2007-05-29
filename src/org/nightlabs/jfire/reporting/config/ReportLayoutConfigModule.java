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

package org.nightlabs.jfire.reporting.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireReporting_ReportLayoutConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.fetch-group name="ReportLayoutConfigModule.availEntries" fields="availEntries"
 */
public class ReportLayoutConfigModule extends ConfigModule {

	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_AVAILABLE_LAYOUTS = "ReportLayoutConfigModule.availEntries";

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
		availEntries = new HashMap<String, ReportLayoutAvailEntry>();
	}
	
	/**
	 * key: String reportRegistryItemType
	 * value: ReportLayoutAvailEntry available entries for this itemType
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.reporting.config.ReportLayoutAvailEntry"
	 *		dependent-value="true"
	 *		mapped-by="reportLayoutConfigModule"
	 *
	 * @jdo.key mapped-by="reportRegistryItemType"
	 */
	private Map<String, ReportLayoutAvailEntry> availEntries;
	
	
	/**
	 * The Map of available entries per reportRegistryItemType 
	 * @return
	 */
	public Map<String, ReportLayoutAvailEntry> getAvailEntries() {
		return availEntries;
	}
	
	public void setAvailEntries(Map<String, ReportLayoutAvailEntry> availEntries) {
		this.availEntries = availEntries;
	}
	
	public ReportLayoutAvailEntry getAvailEntry(String reportRegistryItemType) {
		ReportLayoutAvailEntry result = availEntries.get(reportRegistryItemType);
		if (result == null) {
			result = new ReportLayoutAvailEntry(reportRegistryItemType, this);
			availEntries.put(reportRegistryItemType, result);
		}
		return result;
	}
	
	@Override
	public Object clone() {
		ReportLayoutConfigModule clone = new ReportLayoutConfigModule();
		for (ReportLayoutAvailEntry entry : availEntries.values()) {
			clone.availEntries.put(
					entry.getReportRegistryItemType(),
					new ReportLayoutAvailEntry(
							entry.getReportRegistryItemType(),
							clone
						)
				);
		}
		return clone;
	}
	
	public void assignTo(ReportLayoutConfigModule configModule) {
		configModule.getAvailEntries().clear();
		for (ReportLayoutAvailEntry entry : getAvailEntries().values()) {
			configModule.getAvailEntries().put(
					entry.getReportRegistryItemType(), 
					entry.clone(configModule)
				);
		}
	}
	
	public void copyFrom(ReportLayoutConfigModule configModule) {
		for (ReportLayoutAvailEntry availEntry : configModule.getAvailEntries().values()) {
			ReportLayoutAvailEntry thisEntry = getAvailEntry(availEntry.getReportRegistryItemType());
			if (thisEntry == null)
				throw new IllegalStateException("Could not get ReportLayoutAvailEntry even with auto-create");
			thisEntry.getAvailableReportLayoutKeys().clear();
			for (String availLayout : availEntry.getAvailableReportLayoutKeys()) {
				thisEntry.getAvailableReportLayoutKeys().add(availLayout);
			}
			thisEntry.setDefaultReportLayoutKey(availEntry.getDefaultReportLayoutKey());
		}
	}
	
	public ReportRegistryItemID getDefaultAvailEntry(String reportRegistryItemType) {
		ReportLayoutAvailEntry availEntry = getAvailEntry(reportRegistryItemType);
		if (availEntry == null)
			throw new IllegalStateException("Could not get ReportLayoutAvailEntry even with auto-create");
		return availEntry.getDefaultReportLayoutID();
	}
	
	public Collection<ReportRegistryItemID> getAvailEntries(String reportRegistryItemType) {
		ReportLayoutAvailEntry availEntry = getAvailEntry(reportRegistryItemType);
		if (availEntry == null)
			throw new IllegalStateException("Could not get ReportLayoutAvailEntry even with auto-create");
		return availEntry.getAvailableReportLayoutIDs();		
	}

}
