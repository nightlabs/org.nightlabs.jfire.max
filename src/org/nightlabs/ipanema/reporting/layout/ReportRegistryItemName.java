package org.nightlabs.ipanema.reporting.layout;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.ipanema.reporting.layout.id.ReportRegistryItemNameID"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistryItemName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemID"
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
	 */
	private long reportRegistryItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItem reportRegistryItem;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected ReportRegistryItemName() {
	}

	public ReportRegistryItemName(ReportRegistryItem reportRegistryItem) {
		this.organisationID = reportRegistryItem.getOrganisationID();
		this.reportRegistryItemID = reportRegistryItem.getReportRegistryItemID();
		this.reportRegistryItem = reportRegistryItem;
		this.names = new HashMap();
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
	 *		dependent="true"
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ReportRegistryItemName_names"
	 *
	 * @jdo.join
	 */
	protected Map names;
	
	/**
	 * @see com.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap() {
		return names;
	}

	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, javax.jdo.PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

	/**
	 * @see com.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue) {
		name = localizedValue;
	}

	/**
	 * @see com.nightlabs.i18n.I18nText#getText()
	 */
	public String getText() {
		return name;
	}

	/**
	 * @see com.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID) {
		return reportRegistryItem.getReportRegistryItemType();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	public ReportRegistryItem getReportRegistryItem() {
		return reportRegistryItem;
	}
	
	protected void setReportRegistryItemID(long reportRegistryItemID) {
		this.reportRegistryItemID = reportRegistryItemID;
	}

}