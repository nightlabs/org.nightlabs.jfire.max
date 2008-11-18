/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.Serializable;

/**
 * {@link ReportTextPart}s can be considered as parts of report layouts that can be changed without
 * changing the the report layout (or without having the right to edit reports).
 * <p>
 * {@link ReportTextPart}s are organised in {@link ReportTextPartConfiguration}s and are referenced
 * inside those by their {@link #reportTextPartID} ({@link #getReportTextPartID()}). The configurations
 * are linked to report categories or layout and can also be linked to arbitrary objects. When a report layout
 * is rendered the configuration can be searched using an objectID. This search will then also find
 * the configurations linked to either the currently rendered layout or one of the categories in the
 * hierarchy of its parent. 
 * </p>
 * <p>
 * The content of a {@link ReportTextPart} can be of two {@link Type}s (HTML or JAVASCRIPT) and is
 * converted to a String that can be passed to the javascript <code>eval()</code> method inside
 * a dynamic-text report-item. 
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.textpart.id.ReportTextPartID"
 *		detachable = "true"
 *		table="JFireReporting_ReportTextPart"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportTextPartConfigurationID, reportTextPartID"
 * 
 * @jdo.fetch-group name="ReportTextPart.content" fields="content" 
 * @jdo.fetch-group name="ReportTextPart.name" fields="name"
 * @jdo.fetch-group name="ReportTextPart.reportTextPartConfiguration" fields="reportTextPartConfiguration" 
 * @jdo.fetch-group name="ReportTextPartConfiguration.reportTextParts" fields="reportTextPartConfiguration" 
 * 
 */
public class ReportTextPart implements Serializable {
	
	private static final long serialVersionUID = 20080821L;
	
	public enum Type {
		/**
		 * JSHtml is inside-out javascript with html output. 
		 */
		JSHTML
	}
	
	/**
	 * Fetch-group that will include the {@link ReportTextPartContent}.
	 * Note, that {@link ReportTextPartContent} will ensure that it
	 * includes all fields with this fetch-group, too.
	 */
	public static final String FETCH_GROUP_CONTENT = "ReportTextPart.content";
	/**
	 * Fetch-group that will include the {@link ReportTextPartName}.
	 * Note, that {@link ReportTextPartName} will ensure that it
	 * includes all fields with this fetch-group, too.
	 */
	public static final String FETCH_GROUP_NAME = "ReportTextPart.name";

	/**
	 * Fetch-group that will include the {@link ReportTextPartName}.
	 * Note, that {@link ReportTextPartName} will ensure that it
	 * includes all fields with this fetch-group, too.
	 */
	public static final String FETCH_GROUP_REPORT_TEXT_PART_CONIGURATION = "ReportTextPart.reportTextPartConfiguration";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long reportTextPartConfigurationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportTextPartID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportTextPartConfiguration reportTextPartConfiguration;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Type type;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportTextPart" dependent="true"
	 */
	private ReportTextPartName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportTextPart" dependent="true"
	 */
	private ReportTextPartContent content;

	/**
	 * @deprecated Only for JDO.
	 */
	public ReportTextPart() {}

	public ReportTextPart(ReportTextPartConfiguration reportTextPartConfiguration, String reportTextPartID) {
		this.organisationID = reportTextPartConfiguration.getOrganisationID();
		this.reportTextPartConfigurationID = reportTextPartConfiguration.getReportTextPartConfigurationID();
		this.reportTextPartID = reportTextPartID;
		this.reportTextPartConfiguration = reportTextPartConfiguration;
		this.name = new ReportTextPartName(this);
		this.content = new ReportTextPartContent(this);
		this.type = Type.JSHTML;
	}
	
	/**
	 * @return The organisationID primary-key value of this {@link ReportTextPart}.
	 *         This is equal to the organisationID of the {@link ReportTextPartConfiguration} this type is linked to.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return The teportTextPartConfigurationID primary-key value of this {@link ReportTextPart}.
	 *         This is equal to the reportTextPartConfigurationID of the {@link ReportTextPartConfiguration} this type is linked to.
	 */
	public long getReportTextPartConfigurationID() {
		return reportTextPartConfigurationID;
	}
	
	/**
	 * @return The reportTextPartID primary-key value of this {@link ReportTextPart}.
	 */
	public String getReportTextPartID() {
		return reportTextPartID;
	}

	/**
	 * @return The {@link ReportTextPartConfiguration} this {@link ReportTextPart} is linked to.
	 */
	public ReportTextPartConfiguration getReportTextPartConfiguration() {
		return reportTextPartConfiguration;
	}
	
	/**
	 * Get the {@link Type} of this {@link ReportTextPart}. 
	 * This will be never <code>null</code>. 
	 * 
	 * @return The {@link Type} of this {@link ReportTextPart}, never <code>null</code>.
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Set the Type fo this {@link ReportTextPart}. Note that passing <code>null</code>
	 * here will result in an {@link IllegalArgumentException}.
	 * 
	 * @param type The {@link Type} to set.
	 */
	public void setType(Type type) {
		if (type == null)
			throw new IllegalArgumentException("Type must not be null.");
		this.type = type;
	}
	
	/**
	 * Returns the {@link ReportTextPartName} of this {@link ReportTextPart}.
	 * @return The {@link ReportTextPartName} of this {@link ReportTextPart}.
	 */
	public ReportTextPartName getName() {
		return name;
	}
	
	/**
	 * Returns the {@link ReportTextPartContent} of this {@link ReportTextPart}.
	 * @return The {@link ReportTextPartContent} of this {@link ReportTextPart}.
	 */
	public ReportTextPartContent getContent() {
		return content;
	}
}

