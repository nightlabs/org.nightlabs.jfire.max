package org.nightlabs.jfire.reporting;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.naming.NamingException;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderReportException;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.oda.JFireReportingOdaException;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSQueryProxy;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.version.MalformedVersionException;

@Remote
public interface ReportManagerRemote {

	String ping(String message);

	/**
	 * This method is called by the organisation-init system and is not intended to be called directly.
	 */
	void initialise() throws InstantiationException, IllegalAccessException,
			MalformedVersionException, ScriptingIntialiserException;

	/**
	 * This method is called from a timer-task it is not intended to be called directly.
	 * The method will clean folders temporarily used for reporting.
	 */
	void cleanupRenderedReportLayoutFolders(TaskID taskID) throws Exception;

	/**
	 * Returns the result-set meta-data for the query/script referenced by the given {@link JFSQueryPropertySet}
	 * (its scriptRegistryItemID to be precise). Note, that the properties in the given queryPropertySet might
	 * be necessary to determine the result-set meta-data, this is why this method does not operate on a
	 * {@link ScriptRegistryItemID} but on {@link JFSQueryPropertySet}.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report design-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *
	 * @throws InstantiationException If instantiating the referenced script fails.
	 * @throws ScriptException If creating the meta-data fails.
	 */
	IResultSetMetaData getJFSResultSetMetaData(
			JFSQueryPropertySet queryPropertySet) throws ScriptException,
			InstantiationException;

	/**
	 * Obtains the {@link IJFSQueryPropertySetMetaData} of the referenced script.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report desing-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *
	 * @throws ScriptException If getting the meta-data fails.
	 * @throws InstantiationException If creating the executor fails.
	 */
	IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData(
			ScriptRegistryItemID scriptID) throws ScriptException,
			InstantiationException;

	/**
	 * Executes the script referenced by the given {@link JFSQueryPropertySet} with
	 * the given queryPropertySet and the given parameters.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report desing-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *
	 * @throws InstantiationException If instantiating the script fails.
	 * @throws ScriptException If the script execution fails.
	 */
	IResultSet getJFSResultSet(JFSQueryPropertySet queryPropertySet,
			Map<String, Object> parameters) throws ScriptException,
			InstantiationException;

	/**
	 * Returns the parameter meta-data for the referenced script.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report design-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *
	 * @throws JFireReportingOdaException
	 */
	IParameterMetaData getJFSParameterMetaData(
			ScriptRegistryItemID scriptRegistryItemID)
			throws JFireReportingOdaException;

	/**
	 * Returns the {@link ReportRegistryItem}s represented by the given list of {@link ReportRegistryItemID}s.
	 * All will be detached with the given fetch-groups.
	 *
	 * <p>
	 * This method will filter the result for the given {@link RoleID}, however this is not a real
	 * security check as a caller could call everything here. The security checks
	 * are done in the store methods.
	 * </p>
	 *
	 * @param reportRegistryItemIDs The list of id of items to fetch.
	 * @param filterRoleID The {@link RoleID} to filter the results with.
	 * @param fetchGroups The fetch-groups to detach the items with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 */
	List<ReportRegistryItem> getReportRegistryItems(
			List<ReportRegistryItemID> reportRegistryItemIDs,
			RoleID filterRoleID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link ReportRegistryItem}s represented by the given list of {@link ReportRegistryItemID}s.
	 * All will be detached with the given fetch-groups.
	 * <p>
	 * This method will filter the result for the RoleID {@link RoleConstants#renderReport}
	 * </p>
	 *
	 * @param reportRegistryItemIDs The list of id of items to fetch.
	 * @param fetchGroups The fetch-groups to detach the items with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 */
	List<ReportRegistryItem> getReportRegistryItems(
			List<ReportRegistryItemID> reportRegistryItemIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link ReportRegistryItemID}s of all {@link ReportRegistryItem}s
	 * that are direct children of the given reportRegistryItemID.
	 * <p>
	 * This method will filter the result for the given {@link RoleID}, however this is not a real
	 * security check as a caller could call everything here. The security checks
	 * are done in the store methods.
	 * </p>
	 *
	 * @param reportRegistryItemID The id of the parent to search the children for.
	 */
	Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(
			ReportRegistryItemID reportRegistryItemID, RoleID filterRoleID);

	/**
	 * Returns the {@link ReportRegistryItemID}s of all {@link ReportRegistryItem}s
	 * that are direct children of the given reportRegistryItemID.
	 * <p>
	 * This method will filter the result for the RoleID {@link RoleConstants#renderReport}
	 * </p>
	 *
	 * @param reportRegistryItemID The id of the parent to search the children for.
	 */
	Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(
			ReportRegistryItemID reportRegistryItemID);

	/**
	 * Returns all {@link ReportRegistryItemID}s that do not have a parent.
	 * These will be only for the organisationID of the calling user.
	 * <p>
	 * This method will filter by the given RoleID, however this is not a real
	 * security check as a caller could call everything here. The security checks
	 * are done in the store methods.
	 * </p>
	 *
	 * @param filterRoleID The {@link RoleID} to filter the result for.
	 */
	Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs(
			RoleID filterRoleID);

	/**
	 * Returns all {@link ReportRegistryItemID}s that do not have a parent.
	 * These will be only for the organisationID of the calling user.
	 */
	Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs();

	/**
	 * Stores the given {@link ReportRegistryItem} to the datastore
	 * of the organisation of the calling user.
	 *
	 * @param reportRegistryItemToStore The item to store.
	 * @param get Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups If get is <code>true</code>, this defines the fetch-groups the
	 * 		retuned item will be detached with.
	 * @param maxFetchDepth If get is <code>true</code>, this defines the maximum fetch-depth
	 * 		when detaching.
	 */
	ReportRegistryItem storeRegistryItem(
			ReportRegistryItem reportRegistryItemToStore, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Delete the {@link ReportRegistryItem} with the given id.
	 *
	 * @param reportRegistryItemID The id of the item to delete.
	 */
	void deleteRegistryItem(ReportRegistryItemID reportRegistryItemID);

	/**
	 * Renders the {@link ReportLayout} referenced by the given {@link RenderReportRequest}
	 * and returns the resulting {@link RenderedReportLayout}.
	 *
	 * @param renderReportRequest The request defining which report to render, to which format it
	 * 		should be rendered and which Locale should be applied etc.
	 *
	 * @throws NamingException Might be thrown while resolving the {@link ReportingManagerFactory}.
	 * @throws RenderReportException Might be thrown if rendering the report fails.
	 */
	RenderedReportLayout renderReportLayout(
			RenderReportRequest renderReportRequest) throws NamingException,
			RenderReportException;

	Collection execJDOQL(String jdoql, Map params, String[] fetchGroups);

	/**
	 * Returns the {@link ReportLayoutLocalisationData} for the given report layout id.
	 * The localisation data contains localisation labels for the report.
	 *
	 * @param reportLayoutID The id of the layout to get the localisation data for.
	 * @param fetchGroups The fetch-groups to detach the localisation data with.
	 * @param maxFetchDepth The maximum fetch-depth when detaching.
	 */
	Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(
			ReportRegistryItemID reportLayoutID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Stores the given {@link ReportLayoutLocalisationData} to the datastore
	 * of the organiation of the calling user.
	 *
	 * @param bundle The bundle to store.
	 * @param get Wheter a detached copy of the stored bundle should be returned.
	 * @param fetchGroups If get is <code>true</code>, this defines the fetch-groups the
	 * 		retuned bundle will be detached with.
	 * @param maxFetchDepth If get is <code>true</code>, this defines the maximum fetch-depth
	 * 		when detaching.
	 */
	Collection<ReportLayoutLocalisationData> storeReportLayoutLocalisationBundle(
			Collection<ReportLayoutLocalisationData> bundle, boolean get,
			String[] fetchGroups, int maxFetchDepth);

}