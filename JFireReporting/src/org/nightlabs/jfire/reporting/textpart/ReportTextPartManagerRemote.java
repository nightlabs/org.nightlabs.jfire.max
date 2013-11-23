package org.nightlabs.jfire.reporting.textpart;

import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.textpart.id.ReportTextPartConfigurationID;

@Remote
public interface ReportTextPartManagerRemote {

	String ping(String message);

	/**
	 * This method returns the id of the {@link ReportTextPartConfiguration} linked to
	 * the {@link ReportRegistryItem} referenced by the given reportRegistryItemID.
	 */
	ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID, boolean synthesize,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method returns the id of the {@link ReportTextPartConfiguration} linked to
	 * the {@link ReportRegistryItem} referenced by the given reportRegistryItemID.
	 */
	ReportTextPartConfigurationID getReportTextPartConfigurationID(
			ReportRegistryItemID reportRegistryItemID);

	Set<ReportTextPartConfiguration> getReportTextPartConfigurations(
			Set<ReportTextPartConfigurationID> configurationIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Searches the {@link ReportTextPartConfiguration} for the given linkedObjectID and reportRegistryItemID.
	 * If none can be found in the data-store this method will search for a {@link ReportTextPartConfiguration}
	 * linked to one of the parent {@link ReportCategory}s of the given reportRegistryItemID.
	 * <p>
	 * Additionally this method can be used to get a synthetic, new {@link ReportTextPartConfiguration}
	 * linked to the given linkedObjectID. A synthetic {@link ReportTextPartConfiguration} will have the
	 * values of that one found when searching for the given reportRegistryItem. The synthetic configuration
	 * will not be persisted and only the parts of it referencing existing/persisted objects will be detached.
	 * A synthetic {@link ReportTextPartConfiguration} returned by this methot will therefore not be a
	 * detached object itself, it is rather a newly created object.
	 * </p>
	 *
	 * @param linkedObjectID
	 * 			The {@link ObjectID} a linked {@link ReportTextPartConfiguration} should be found for.
	 * @param reportRegistryItemID
	 * 			The {@link ReportRegistryItemID} to start the search for a {@link ReportTextPartConfiguration}
	 * 			that is linked to a {@link ReportRegistryItem} should be started from.
	 * @param synthesize
	 * 			Whether to synthesize a new {@link ReportTextPartConfiguration} when none directly linked to the
	 * 			given linkedObjectID was found but one was found linked to a reportRegistryItem.
	 * @param fetchGroups
	 * 			The fetch-groups to detach the found {@link ReportTextPartConfiguration} with. Note, that this
	 * 			fetch-groups will also be used used when synthesizing a new configuration, but then to detach those
	 * 			parts of the the found configuration that reference already persisted object.
	 * @param maxFetchDepth
	 * 			The maximum fetch-depth to detach the found {@link ReportTextPartConfiguration} with. Note, that this
	 * 			fetch-depth will also be used used when synthesizing a new configuration, but then to detach those
	 * 			parts of the the found configuration that reference already persisted object.
	 * @return
	 * 			The {@link ReportTextPartConfiguration} either found (or synthesized) for the given linkedObjectID
	 * 			or the configuration found for a {@link ReportRegistryItem}. If nothing can be found, <code>null</code> will be returned.
	 */
	ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID,
			boolean synthesize, String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method stores the given {@link ReportTextPartConfiguration} if it is
	 * a configuration linked to an object in the datastore.
	 *
	 * @param reportTextPartConfiguration
	 * 			The configuration to store.
	 * @param get
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 */
	ReportTextPartConfiguration storeLinkedObjectReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration,
			boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method stores the given {@link ReportTextPartConfiguration}.
	 *
	 * @param reportTextPartConfiguration
	 * 			The configuration to store.
	 * @param get
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 */
	ReportTextPartConfiguration storeReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration,
			boolean get, String[] fetchGroups, int maxFetchDepth);

}