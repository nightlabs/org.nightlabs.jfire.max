package org.nightlabs.jfire.reporting.parameter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import org.nightlabs.jfire.reporting.parameter.dao.ReportParameterAcquisitionSetupDAO;
import org.nightlabs.jfire.reporting.parameter.dao.ValueProviderDAO;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

@Remote
public interface ReportParameterManagerRemote {

	/**
	 * This method is called by the organisation-init system, it is not intended to be called directly.
	 * <p>
	 * This initializes the default value providers for simple data-types.
	 * </p>
	 */
	void initDefaultValueProviders();

	/**
	 * This method returns detached copies of the {@link ValueProvider}s for the given ids.
	 * {@link ValueProviderDAO} uses this method, use the dao rather than using this method directly.
	 */
	Set<ValueProvider> getValueProviders(Set<ValueProviderID> providerIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the ids of the {@link ReportParameterAcquisitionSetup}s mapped to the given report layouts.
	 * Note, that for those report layouts with no {@link ReportParameterAcquisitionSetup} linked to them,
	 * this report layout id will be returned mapping to <code>null</code>.
	 * <p>
	 * This method in combination with {@link #getReportParameterAcquisitionSetups(Set, String[], int)} is
	 * used by {@link ReportParameterAcquisitionSetupDAO}. Use the dao rather than using this method directly.
	 * </p>
	 */
	Map<ReportRegistryItemID, ReportParameterAcquisitionSetupID> getReportParameterAcquisitionSetupIDs(Collection<ReportRegistryItemID> reportLayoutIDs);

	/**
	 * Returns detached copies of the {@link ReportParameterAcquisitionSetup}s referenced by the given ids.
	 * This is used by {@link ReportParameterAcquisitionSetupDAO}, use the dao rather than this method directly.
	 */
	Set<ReportParameterAcquisitionSetup> getReportParameterAcquisitionSetups(Set<ReportParameterAcquisitionSetupID> setupIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Searches for the ids of the {@link ValueProviderCategory}ies that are children of the category
	 * referenced by the given valueProviderCategoryID.
	 */
	Set<ValueProviderCategoryID> getValueProviderCategoryIDsForParent(ValueProviderCategoryID valueProviderCategoryID);

	Set<ValueProviderCategory> getValueProviderCategories(Set<ValueProviderCategoryID> categoryIDs, String[] fetchGroups, int maxFetchDepth);

	Set<ValueProviderID> getValueProviderIDsForParent(ValueProviderCategoryID valueProviderCategoryID);

	/**
	 * Stores the given {@link ReportParameterAcquisitionSetup}.
	 */
	ReportParameterAcquisitionSetup storeReportParameterAcquisitionSetup(ReportParameterAcquisitionSetup setup, boolean get, String[] fetchGroups, int maxFetchDepth);

}