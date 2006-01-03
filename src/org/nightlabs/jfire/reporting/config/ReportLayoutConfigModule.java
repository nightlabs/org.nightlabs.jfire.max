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
