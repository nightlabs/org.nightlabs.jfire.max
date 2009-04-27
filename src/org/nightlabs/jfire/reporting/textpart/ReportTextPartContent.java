/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import org.nightlabs.jfire.reporting.textpart.id.ReportTextPartContentID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} holding the content of an {@link ReportTextPart}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.textpart.id.ReportTextPartContentID"
 *		detachable = "true"
 *		table="JFireReporting_ReportTextPartContent"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportTextPartConfigurationID, reportTextPartID"
 * 
 * @jdo.fetch-group name="ReportTextPart.content" fields="reportTextPart, contents" 
 */
@PersistenceCapable(
	objectIdClass=ReportTextPartContentID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportTextPartContent")
@FetchGroups(
	@FetchGroup(
		name="ReportTextPart.content",
		members={@Persistent(name="reportTextPart"), @Persistent(name="contents")})
)
public class ReportTextPartContent extends I18nText implements Serializable {
	
	private static final long serialVersionUID = 20080821L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long reportTextPartConfigurationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String reportTextPartID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ReportTextPart reportTextPart;
	
	/**
	 * key: String languageID</br>
	 * value: String contents
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ReportTextPartContent_contents"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		table="JFireReporting_ReportTextPartContent_contents",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> contents = new HashMap<String, String>();
	
	
	/**
	 * @deprecated Only for JDO.
	 */
	public ReportTextPartContent() {}

	public ReportTextPartContent(ReportTextPart reportTextPart) {
		this.organisationID = reportTextPart.getOrganisationID();
		this.reportTextPartConfigurationID = reportTextPart.getReportTextPartConfigurationID();
		this.reportTextPartID = reportTextPart.getReportTextPartID();
		this.reportTextPart = reportTextPart;
	}
	
	/**
	 * @return The organisationID primary-key value of this {@link ReportTextPart}.
	 *         This is equal to the organisationID of the {@link ReportTextPart} this type is linked to.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return The teportTextPartConfigurationID primary-key value of this {@link ReportTextPart}.
	 *         This is equal to the reportTextPartConfigurationID of the {@link ReportTextPart} this type is linked to.
	 */
	public long getReportTextPartConfigurationID() {
		return reportTextPartConfigurationID;
	}
	
	/**
	 * @return The reportTextPartTypeID primary-key value of this {@link ReportTextPart}.
	 *         This is equal to the reportTextPartID of the {@link ReportTextPart} this type is linked to.
	 */
	public String getReportTextPartID() {
		return reportTextPartID;
	}
	
	/**
	 * @return The {@link ReportTextPart} of this {@link ReportTextPart}.
	 */
	public ReportTextPart getReportTextPart() {
		return reportTextPart;
	}
	
	@Override
	protected String getFallBackValue(String languageID) {
		return reportTextPartID;
	}

	@Override
	protected Map<String, String> getI18nMap() {
		return contents;
	}

}
