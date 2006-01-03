/**
 * 
 */
package org.nightlabs.jfire.reporting.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * ConfigModule to store all available and one default 
 * ReportLayouts for one reportCategoryType.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[ÐOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.config.id.ReportLayoutAvailEntryID"
 *		detachable = "true"
 *		table="JFireReporting_ReportLayoutAvailEntry"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemID"
 */
public class ReportLayoutAvailEntry implements Serializable {

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportLayoutConfigModule reportLayoutConfigModule;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
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
	 */
	private long reportRegistryItemID;
	
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
	public ReportLayoutAvailEntry(String reportRegistryItemType, long reportRegistryItemID, ReportLayoutConfigModule configModule) {
		this();
		this.reportLayoutConfigModule = configModule;
		this.reportRegistryItemType = reportRegistryItemType;
		this.reportRegistryItemID =  reportRegistryItemID;
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
	 * @return Returns the reportRegistryItemID.
	 */
	public long getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * @param reportRegistryItemID The reportRegistryItemID to set.
	 */
	public void setReportRegistryItemID(long reportRegistryItemID) {
		this.reportRegistryItemID = reportRegistryItemID;
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
	
}
