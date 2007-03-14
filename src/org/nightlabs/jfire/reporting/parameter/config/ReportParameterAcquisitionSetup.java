/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

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
 * @jdo.fetch-group name="ReportParameterAcquisitionSetup.defaultSetup" fetch-groups="default" fields="defaultSetup"
 * @jdo.fetch-group name="ReportParameterAcquisitionSetup.reportLayout" fetch-groups="default" fields="reportLayout"
 * @jdo.fetch-group name="ReportParameterAcquisitionSetup.valueAcquisitionSetups" fetch-groups="default" fields="valueAcquisitionSetups"
 * @jdo.fetch-group name="ReportParameterAcquisitionSetup.this" fetch-groups="default" fields="defaultSetup, reportLayout, valueAcquisitionSetups"
 * 
 * @jdo.query
 * 		name="getSetupForReportLayout"
 *		query="SELECT UNIQUE
 *			WHERE 
 *				this.reportLayout.organisationID == paramOrganisationID &&
 *				this.reportLayout.reportRegistryItemType == paramRegistryItemType &&
 *				this.reportLayout.reportRegistryItemID == paramRegistryItemID
 *			PARAMETERS String paramOrganisationID, String paramRegistryItemType, String paramRegistryItemID
 *			import java.lang.String"
 * 
 */
public class ReportParameterAcquisitionSetup implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_DEFAULT_SETUP = "ReportParameterAcquisitionSetup.defaultSetup";
	public static final String FETCH_GROUP_REPORT_LAYOUT = "ReportParameterAcquisitionSetup.reportLayout";
	public static final String FETCH_GROUP_VALUE_ACQUISITION_SETUPS = "ReportParameterAcquisitionSetup.valueAcquisitionSetups";
	public static final String FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_SETUP = "ReportParameterAcquisitionSetup.this";
			 
			 
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
	 *		mapped-by="parameterAcquisitionSetup"
	 *		dependent-key="true"
	 *		dependent-value="true"
	 *
	 * @jdo.key
	 * 		mapped-by="useCase"
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
	
	public static ReportParameterAcquisitionSetup getSetupForReportLayout(PersistenceManager pm, ReportRegistryItemID reportRegistryItemID) {
		Query q = pm.newNamedQuery(ReportParameterAcquisitionSetup.class, "getSetupForReportLayout");
		return (ReportParameterAcquisitionSetup) q.execute(reportRegistryItemID.organisationID, reportRegistryItemID.reportRegistryItemType, reportRegistryItemID.reportRegistryItemID);
	}
}
