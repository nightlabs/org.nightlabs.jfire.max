/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.reporting.layout.ReportLayout;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID"
 *		detachable = "true"
 *		table="JFireReporting_ReportParameterAcquisitionSetup"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportParameterAcquisitionSetupID"
 *
 */
public class ReportParameterAcquisitionSetup {

	
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueAcquisitionSetup defaultSetup;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportLayout reportLayout; 

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase"
	 *		value-type="org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup"
	 *		table="JFireReporting_ReportParameterAcquisitionSetup_valueAcquisitionSetups"
	 *		dependent-key="true"
	 *		dependent-value="true"
	 *
	 * @jdo.join
	 */
	private Map<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> valueAcquisitionSetups;
	

//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase"
//	 *		mapped-by="setup"
//	 *		dependent-element="true"
//	 */
//	private List<ReportParameterAcquisitionUseCase> useCases;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ReportParameterAcquisitionSetup() {
		
	}
	
	/**
	 * 
	 */
	public ReportParameterAcquisitionSetup(String organisationID, long reportParameterAcquisitionSetupID, ReportLayout reportLayout) {
		this.organisationID = organisationID;
		this.reportParameterAcquisitionSetupID = reportParameterAcquisitionSetupID;
		this.valueAcquisitionSetups = new HashMap<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup>();
		this.reportLayout = reportLayout;
	}


	/**
	 * @return the defaultSetup
	 */
	public ValueAcquisitionSetup getDefaultSetup() {
		return defaultSetup;
	}


	/**
	 * @param defaultSetup the defaultSetup to set
	 */
	public void setDefaultSetup(ValueAcquisitionSetup defaultSetup) {
		this.defaultSetup = defaultSetup;
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
	 * @return the valueAcquisitionSetups
	 */
	public Map<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> getValueAcquisitionSetups() {
		return valueAcquisitionSetups;	
	}

	/**
	 * @return the reportLayout
	 */
	public ReportLayout getReportLayout() {
		return reportLayout;
	}
}
