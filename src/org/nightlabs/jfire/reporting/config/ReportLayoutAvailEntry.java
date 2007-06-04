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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * ConfigModule to store all available and one default 
 * ReportLayouts for one reportCategoryType.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.config.id.ReportLayoutAvailEntryID"
 *		detachable = "true"
 *		table="JFireReporting_ReportLayoutAvailEntry"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="ReportLayoutAvailEntry.availableReportLayoutKeys" fields="availableReportLayoutKeys"
 */
public class ReportLayoutAvailEntry 
implements Serializable 
{
	public static final String FETCH_GROUP_AVAILABLE_REPORT_LAYOUT_KEYS = "ReportLayoutAvailEntry.availableReportLayoutKeys";
	
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportLayoutConfigModule reportLayoutConfigModule;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String configKey;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	protected String configType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="150"
	 */
	private String cfModKey;
	
	/**
	 * The default ReportLayoutID for this categoryType
	 */	
	private String defaultReportLayoutKey;
	
	/**
	 * Collection of all ReportLayoutIDs available
	 * for this categoryType.
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="java.lang.String"
	 *		table="JFireReporting_ReportLayoutAvailEntry_availableReportLayoutKeys"
	 *
	 * @jdo.join
	 */
	private Collection<String> availableReportLayoutKeys;
	
	public ReportLayoutAvailEntry() {
		defaultReportLayoutKey = null;
		availableReportLayoutKeys = new HashSet<String>();
	}
	
	/**
	 * 
	 */
	public ReportLayoutAvailEntry(String reportRegistryItemType, ReportLayoutConfigModule configModule) {
		this();
		this.reportLayoutConfigModule = configModule;
		this.reportRegistryItemType = reportRegistryItemType;
		this.organisationID = configModule.getOrganisationID();
		this.configKey = configModule.getConfigKey();
		this.configType = configModule.getConfigType();
		this.cfModKey = configModule.getCfModKey();
	}
	
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @param organisationID The organisationID to set.
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	
	/**
	 * @return Returns the reportRegistryItemType.
	 */
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	/**
	 * @param reportRegistryItemType The reportRegistryItemType to set.
	 */
	public void setReportRegistryItemType(String reportRegistryItemType) {
		this.reportRegistryItemType = reportRegistryItemType;
	}
	
	/**
	 * @return Returns the reportLayoutConfigModule.
	 */
	public ReportLayoutConfigModule getReportLayoutConfigModule() {
		return reportLayoutConfigModule;
	}
	
	/**
	 * @param reportLayoutConfigModule The reportLayoutConfigModule to set.
	 */
	public void setReportLayoutConfigModule(
			ReportLayoutConfigModule reportLayoutConfigModule) {
		this.reportLayoutConfigModule = reportLayoutConfigModule;
	}
	
	/**
	 * @return Returns the availableReportLayouts.
	 */
	public Collection<String> getAvailableReportLayoutKeys() {
		return availableReportLayoutKeys;
	}
	
	/**
	 * @param availableReportLayouts The availableReportLayouts to set.
	 */
	public void setAvailableReportLayoutKeys(Collection<String> availableReportLayoutKeys) {
		this.availableReportLayoutKeys = availableReportLayoutKeys;
	}
	
	/**
	 * @return Returns the defaultReportLayoutKey.
	 */
	public String getDefaultReportLayoutKey() {
		return defaultReportLayoutKey;
	}
	
	/**
	 * @param defaultReportLayoutID The defaultReportLayoutID to set.
	 */
	public void setDefaultReportLayoutKey(String defaultReportLayoutKey) {
		this.defaultReportLayoutKey = defaultReportLayoutKey;
	}
	
	/**
	 * Returns either the ReportRegistryItemID configured as default for 
	 * this entry or null if none is set.
	 */
	public ReportRegistryItemID getDefaultReportLayoutID() {
		if (defaultReportLayoutKey == null)
			return null;
		try {
			return new ReportRegistryItemID(defaultReportLayoutKey);
		} catch (Exception e) {
			throw new IllegalStateException("Could not create ReportRegistryItemID instance out of string-key: "+defaultReportLayoutKey, e);
		}
	}
	
	/**
	 * Converts the stored Collection of strings into a Collection
	 * of ReportRegistryItemIDs representing the available 
	 * ReportLayouts in this entry.
	 */
	public Collection<ReportRegistryItemID> getAvailableReportLayoutIDs() {
		Collection<ReportRegistryItemID> result = new HashSet<ReportRegistryItemID>();
		for (String key : availableReportLayoutKeys) {
			try {
				result.add(new ReportRegistryItemID(key));
			} catch (Exception e) {
				throw new IllegalStateException("Could not create ReportRegistryItemID instance out of string-key: "+key, e);
			}
		}
		return result;
	}

	/**
	 * @return Returns the cfModKey.
	 */
	public String getCfModKey() {
		return cfModKey;
	}

	/**
	 * @param cfModKey The cfModKey to set.
	 */
	public void setCfModKey(String cfModKey) {
		this.cfModKey = cfModKey;
	}

	/**
	 * @return Returns the configKey.
	 */
	public String getConfigKey() {
		return configKey;
	}

	/**
	 * @param configKey The configKey to set.
	 */
	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	/**
	 * @return Returns the configType.
	 */
	public String getConfigType() {
		return configType;
	}

	/**
	 * @param configType The configType to set.
	 */
	public void setConfigType(String configType) {
		this.configType = configType;
	}

	public ReportLayoutAvailEntry clone(ReportLayoutConfigModule configModule) {
		ReportLayoutAvailEntry clone = new ReportLayoutAvailEntry(getReportRegistryItemType(), configModule);
		clone.setDefaultReportLayoutKey(getDefaultReportLayoutKey());
		for (String availKey : getAvailableReportLayoutKeys()) {
			clone.getAvailableReportLayoutKeys().add(availKey);
		}
		return clone;
	}
	
}
