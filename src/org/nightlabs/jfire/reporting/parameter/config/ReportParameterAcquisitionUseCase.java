/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

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
 */
public class ReportParameterAcquisitionUseCase {

	public static final String FETCH_GROUP_NAME = "ReportParameterAcquisitionUseCase.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ReportParameterAcquisitionUseCase.description";
	public static final String FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_USE_CASE = "ReportParameterAcquisitionUseCase.this";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long reportParameterAcquisitionSetupID;	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportParameterAcquisitionUseCaseID;
		
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionSetup setup;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionUseCaseName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionUseCaseDescription description;
	
	
	/**
	 * 
	 */
	public ReportParameterAcquisitionUseCase(ReportParameterAcquisitionSetup setup, String reportParameterAcquisitionUseCaseID) {
		this.organisationID = setup.getOrganisationID();
		this.reportParameterAcquisitionSetupID = setup.getReportParameterAcquisitionSetupID();
		this.reportParameterAcquisitionUseCaseID = reportParameterAcquisitionUseCaseID;
		this.setup = setup;
		this.name = new ReportParameterAcquisitionUseCaseName(this);
		this.description = new ReportParameterAcquisitionUseCaseDescription(this);
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


	/**
	 * @return the setup
	 */
	public ReportParameterAcquisitionSetup getSetup() {
		return setup;
	}


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
	
}
