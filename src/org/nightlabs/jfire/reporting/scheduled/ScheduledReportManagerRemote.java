/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import java.util.Collection;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@Remote
public interface ScheduledReportManagerRemote {
	
	String ping(String message);
	
	void initialise();

	/**
	 * Get a detached copy of the referenced {@link ScheduledReport}
	 * detached with the given fetch-groups and fetch-depth.
	 *  
	 * @param scheduledReportID The id of the {@link ScheduledReport} to return.
	 * @param fetchGroups The fetch-groups to detach the {@link ScheduledReport} with.
	 * @param maxFetchDepth The maximum fetch-depth to detach the {@link ScheduledReport} with.
	 * @return A detached copy of the referenced {@link ScheduledReport}.
	 */
	ScheduledReport getScheduledReport(ScheduledReportID scheduledReportID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get a detached copies of the referenced {@link ScheduledReport}s
	 * detached with the given fetch-groups and fetch-depth.
	 *  
	 * @param scheduledReportIDs The ids of the {@link ScheduledReport} to return.
	 * @param fetchGroups The fetch-groups to detach the {@link ScheduledReport} with.
	 * @param maxFetchDepth The maximum fetch-depth to detach the {@link ScheduledReport} with.
	 * @return Detached copies of the referenced {@link ScheduledReport}.
	 */
	Collection<ScheduledReport> getScheduledReports(Set<ScheduledReportID> scheduledReportIDs,
			String[] fetchGroups, int maxFetchDepth);
	
	/**
	 * Get the list of stored {@link ScheduledReport}s owned by the current user.
	 * 
	 * @return The list of stored {@link ScheduledReport}s owned by the current user.
	 */
	Collection<ScheduledReportID> getScheduledReportIDs();
	
	/**
	 * Store/Persist the given {@link ScheduledReport}.
	 * If <code>get</code> is true, a detached copy of the new version
	 * of the {@link ScheduledReport} will be returned, <code>null</code> otherwise.
	 *  
	 * @param scheduledReport The {@link ScheduledReport} to store.
	 * @param get Whether to get a detached copy of the new version.
	 * @param fetchGroups The fetch-groups to detach the new version with, when <code>get</code> is <code>true</code>.
	 * @param maxFetchDepth The fetch-depth to detach the new version with, when <code>get</code> is <code>true</code>.
	 * @return Either a detached copy of the new version of the given {@link ScheduledReport}, 
	 * 		or <code>null</code> if <code>get</code> is <code>false</code>.  
	 */
	ScheduledReport storeScheduledReport(ScheduledReport scheduledReport, boolean get,
			String[] fetchGroups, int maxFetchDepth);
	

	/**
	 * Deletes the {@link ScheduledReport} with the given {@link ScheduledReportID}.
	 * 
	 * @param scheduledReportID The id of the {@link ScheduledReport} to delete.
	 */
	void deleteScheduledReport(ScheduledReportID scheduledReportID);

}
