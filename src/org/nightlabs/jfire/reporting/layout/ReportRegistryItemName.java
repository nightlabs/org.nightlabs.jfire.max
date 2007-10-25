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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemNameID"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistryItemName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemType, reportRegistryItemID"
 *
 * @jdo.fetch-group name="ReportRegistryItem.name" fields="reportRegistryItem, names"
 */
public class ReportRegistryItemName extends I18nText {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItem reportRegistryItem;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ReportRegistryItemName() {
	}

	public ReportRegistryItemName(ReportRegistryItem reportRegistryItem) {
		this.organisationID = reportRegistryItem.getOrganisationID();
		this.reportRegistryItemID = reportRegistryItem.getReportRegistryItemID();
		this.reportRegistryItemType = reportRegistryItem.getReportRegistryItemType();
		this.reportRegistryItem = reportRegistryItem;
		this.names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ReportRegistryItemName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names;
	
	/**
	 * @see com.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	/**
	 * @see com.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return reportRegistryItem.getReportRegistryItemType();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	public ReportRegistryItem getReportRegistryItem() {
		return reportRegistryItem;
	}
	
}
