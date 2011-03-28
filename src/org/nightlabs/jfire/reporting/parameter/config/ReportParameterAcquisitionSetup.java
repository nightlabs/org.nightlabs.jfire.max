/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.listener.DeleteCallback;

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
 * @jdo.fetch-group name="ReportParameterAcquisitionSetup.defaultUseCase" fetch-groups="default" fields="defaultUseCase"
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
 */@PersistenceCapable(
	objectIdClass=ReportParameterAcquisitionSetupID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportParameterAcquisitionSetup")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionSetup.FETCH_GROUP_DEFAULT_USE_CASE,
		members=@Persistent(name="defaultUseCase")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionSetup.FETCH_GROUP_REPORT_LAYOUT,
		members=@Persistent(name="reportLayout")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionSetup.FETCH_GROUP_VALUE_ACQUISITION_SETUPS,
		members=@Persistent(name="valueAcquisitionSetups")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ReportParameterAcquisitionSetup.FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_SETUP,
		members={@Persistent(name="defaultSetup"), @Persistent(name="reportLayout"), @Persistent(name="valueAcquisitionSetups")})
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getSetupForReportLayout",
		value="SELECT UNIQUE WHERE this.reportLayout.organisationID == paramOrganisationID && this.reportLayout.reportRegistryItemType == paramRegistryItemType && this.reportLayout.reportRegistryItemID == paramRegistryItemID PARAMETERS String paramOrganisationID, String paramRegistryItemType, String paramRegistryItemID import java.lang.String")
)

public class ReportParameterAcquisitionSetup
implements Serializable, DeleteCallback
{

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_DEFAULT_USE_CASE = "ReportParameterAcquisitionSetup.defaultUseCase";
	public static final String FETCH_GROUP_REPORT_LAYOUT = "ReportParameterAcquisitionSetup.reportLayout";
	public static final String FETCH_GROUP_VALUE_ACQUISITION_SETUPS = "ReportParameterAcquisitionSetup.valueAcquisitionSetups";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_REPORT_PARAMETER_ACQUISITION_SETUP = "ReportParameterAcquisitionSetup.this";
			 
			 
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
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ReportParameterAcquisitionUseCase defaultUseCase;
//	private ValueAcquisitionSetup defaultSetup;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ReportLayout reportLayout;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase"
	 *		value-type="org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup"
	 *		mapped-by="parameterAcquisitionSetup"
	 *		dependent-value="true"
	 *
	 * @jdo.key
	 * 		mapped-by="useCase"
	 */	@Persistent(
		mappedBy="parameterAcquisitionSetup",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="useCase")
	@Value(dependent="true")

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
	@Deprecated
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
		if (defaultUseCase == null)
			return null;

		return valueAcquisitionSetups.get(defaultUseCase);
//		return defaultSetup;
	}

	/**
	 * @param defaultSetup the defaultSetup to set
	 */
	public void setDefaultSetup(ValueAcquisitionSetup defaultSetup) {
		this.defaultUseCase = defaultSetup != null ? defaultSetup.getUseCase() : null;
//		this.defaultSetup = defaultSetup;
	}

	public ReportParameterAcquisitionUseCase getDefaultUseCase() {
		return defaultUseCase;
	}
	public void setDefaultUseCase(
			ReportParameterAcquisitionUseCase defaultUseCase) {
		this.defaultUseCase = defaultUseCase;
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
		return Collections.unmodifiableMap(valueAcquisitionSetups);
	}

	public void addValueAcquisitionSetup(ValueAcquisitionSetup valueAcquisitionSetup)
	{
		valueAcquisitionSetups.put(valueAcquisitionSetup.getUseCase(), valueAcquisitionSetup);
	}

	/**
	 * Remove the use case and its ValueAcquisitionSetup.
	 * @param useCase
	 */
	public void removeUseCase(ReportParameterAcquisitionUseCase useCase)
	{
		assert useCase != null;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);

		if (useCase.equals(defaultUseCase)) {
			// TODO we should set the next available use-case as default
			defaultUseCase = null;
			if (pm != null)
				pm.flush();
		}
		valueAcquisitionSetups.remove(useCase);
//		ValueAcquisitionSetup setup = valueAcquisitionSetups.remove(useCase);
//		if (pm != null) {
//			pm.deletePersistent(setup);
//			pm.deletePersistent(useCase);
//			pm.flush();
//		}
	}
	
	@Override
	public void jdoPreDelete() {
		if(defaultUseCase != null)
		{
			removeUseCase(defaultUseCase);
		}
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
