/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;

import org.nightlabs.util.Util;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionUseCaseID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionUseCaseID"
 *		detachable = "true"
 *		table="JFireReporting_ReportParameterAcquisitionUseCase"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportParameterAcquisitionSetupID, reportParameterAcquisitionUseCaseID"
 *
 * @jdo.fetch-group name="ReportParameterAcquisitionUseCase.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ReportParameterAcquisitionUseCase.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="ReportParameterAcquisitionUseCase.this" fetch-groups="default" fields="name, description"
 *
 */@PersistenceCapable(
	objectIdClass=ReportParameterAcquisitionUseCaseID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportParameterAcquisitionUseCase")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionUseCase.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionUseCase.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionUseCase.FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_USE_CASE,
		members={@Persistent(name="name"), @Persistent(name="description")})
})

public class ReportParameterAcquisitionUseCase implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_NAME = "ReportParameterAcquisitionUseCase.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ReportParameterAcquisitionUseCase.description";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_USE_CASE = "ReportParameterAcquisitionUseCase.this";
			 

	public static final String USE_CASE_ID_DEFAULT = "DefaultUseCase";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long reportParameterAcquisitionSetupID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String reportParameterAcquisitionUseCaseID;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private ReportParameterAcquisitionSetup parameterAcquisitionSetup;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="useCase"
	 *		dependent="true"
	 */@Persistent(
	dependent="true",
	mappedBy="useCase",
	persistenceModifier=PersistenceModifier.PERSISTENT)

	private ReportParameterAcquisitionUseCaseName name;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="useCase"
	 *		dependent="true"
	 */	@Persistent(
		dependent="true",
		mappedBy="useCase",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private ReportParameterAcquisitionUseCaseDescription description;

	/**
	 * 
	 */
	public ReportParameterAcquisitionUseCase(ReportParameterAcquisitionSetup setup, String reportParameterAcquisitionUseCaseID) {
		this.organisationID = setup.getOrganisationID();
		this.reportParameterAcquisitionSetupID = setup.getReportParameterAcquisitionSetupID();
		this.reportParameterAcquisitionUseCaseID = reportParameterAcquisitionUseCaseID;
		this.name = new ReportParameterAcquisitionUseCaseName(this);
		this.description = new ReportParameterAcquisitionUseCaseDescription(this);
//		this.parameterAcquisitionSetup = setup;
	}


	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}


	/**
	 * @return the reportParameterAcquisitionSetupID
	 */
	public long getReportParameterAcquisitionSetupID() {
		return reportParameterAcquisitionSetupID;
	}


	/**
	 * @return the reportParameterAcquisitionUseCaseID
	 */
	public String getReportParameterAcquisitionUseCaseID() {
		return reportParameterAcquisitionUseCaseID;
	}


//	/**
//	 * @return the setup
//	 */
//	public ReportParameterAcquisitionSetup getParameterAcquisitionSetup() {
//		return parameterAcquisitionSetup;
//	}


	/**
	 * @return the description
	 */
	public ReportParameterAcquisitionUseCaseDescription getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(
			ReportParameterAcquisitionUseCaseDescription description) {
		this.description = description;
	}


	/**
	 * @return the name
	 */
	public ReportParameterAcquisitionUseCaseName getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(ReportParameterAcquisitionUseCaseName name) {
		this.name = name;
	}


	/** {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime
				* result
				+ (int) (reportParameterAcquisitionSetupID ^ (reportParameterAcquisitionSetupID >>> 32));
		result = prime
				* result
				+ ((reportParameterAcquisitionUseCaseID == null) ? 0
						: reportParameterAcquisitionUseCaseID.hashCode());
		return result;
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ReportParameterAcquisitionUseCase other = (ReportParameterAcquisitionUseCase) obj;
		return
				Util.equals(organisationID, other.organisationID) &&
				Util.equals(reportParameterAcquisitionSetupID, other.reportParameterAcquisitionSetupID) &&
				Util.equals(reportParameterAcquisitionUseCaseID, other.reportParameterAcquisitionUseCaseID);
	}
}
