/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.Serializable;

/**
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
 */
public class ReportTextPart implements Serializable {
	
	private static final long serialVersionUID = 20080821L;
	
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
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportTextPartType" dependent="true"
	 */
	private ReportTextPartName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportTextPartType" dependent="true"
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

