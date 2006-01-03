/**
 * 
 */
package org.nightlabs.jfire.reporting.config;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[ÐOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireReporting_ReportLayoutConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ReportLayoutConfigModule extends ConfigModule {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
	}
	
	public ReportLayoutConfigModule() {
		super();
	}
	
	public ReportLayoutConfigModule(String organisationID, Config config, String cfModID) {
		super(organisationID, config, cfModID);
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
	 *		dependent="true"
	 *		mapped-by="reportLayoutConfigModule"
	 *
	 * @jdo.key mapped-by="reportRegistryItemType"
	 */
	private Map<String, ReportLayoutAvailEntry> availEntries = new HashMap<String, ReportLayoutAvailEntry>();;
	
	
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
	
}
