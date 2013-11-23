/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import org.nightlabs.jfire.reporting.textpart.id.ReportTextPartNameID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.textpart.id.ReportTextPartNameID"
 *		detachable = "true"
 *		table="JFireReporting_ReportTextPartName"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportTextPartConfigurationID, reportTextPartID"
 * 
 * @jdo.fetch-group name="ReportTextPartName.names" fields="names"
 * @jdo.fetch-group name="ReportTextPart.name" fields="reportTextPart, names"
 * 
 */
@PersistenceCapable(
	objectIdClass=ReportTextPartNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportTextPartName")
@FetchGroups({
	@FetchGroup(
		name="ReportTextPartName.names",
		members=@Persistent(name="names")),
	@FetchGroup(
		name="ReportTextPart.name",
		members={@Persistent(name="reportTextPart"), @Persistent(name="names")})
})
public class ReportTextPartName extends I18nText {

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
	 * key: String languageID<br/>
	 * value: String names
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ReportTextPartName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireReporting_ReportTextPartName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();
	
	
	/**
	 * @deprecated Only for JDO.
	 */
	public ReportTextPartName() {}

	/**
	 * Create a new {@link ReportTextPartName} for the given {@link ReportTextPart}.
	 * 
	 * @param reportTextPart The {@link ReportTextPart} the new name is for.
	 */
	public ReportTextPartName(ReportTextPart reportTextPart) {
		this.organisationID = reportTextPart.getOrganisationID();
		this.reportTextPartConfigurationID = reportTextPart.getReportTextPartConfigurationID();
		this.reportTextPartID = reportTextPart.getReportTextPartID();
		this.reportTextPart = reportTextPart;
	}
	
	/**
	 * @return The organisationID primary-key value of this {@link ReportTextPartName}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return The reportTextPartConfigurationID primary-key value of this {@link ReportTextPartName}.
	 */
	public long getReportTextPartConfigurationID() {
		return reportTextPartConfigurationID;
	}
	
	/**
	 * @return The reportTextPartID primary-key value of this {@link ReportTextPartName}.
	 */
	public String getReportTextPartID() {
		return reportTextPartID;
	}
	
	/**
	 * @return The {@link ReportTextPart} of this {@link ReportTextPart}.
	 */
	public ReportTextPart getReportTextPartType() {
		return reportTextPart;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return reportTextPartID;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

}
