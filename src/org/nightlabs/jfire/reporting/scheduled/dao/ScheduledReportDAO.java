/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.reporting.scheduled.ScheduledReport;
import org.nightlabs.jfire.reporting.scheduled.ScheduledReportManagerRemote;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data access object for {@link ScheduledReport}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ScheduledReportDAO 
extends BaseJDOObjectDAO<ScheduledReportID, ScheduledReport> 
implements IJDOObjectDAO<ScheduledReport> {

	/**
	 * Create a new {@link ScheduledReportDAO}. 
	 */
	public ScheduledReportDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<? extends ScheduledReport> retrieveJDOObjects(
			Set<ScheduledReportID> scheduledReportIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		return JFireEjb3Factory.getRemoteBean(ScheduledReportManagerRemote.class, SecurityReflector.getInitialContextProperties())
			.getScheduledReports(scheduledReportIDs, fetchGroups, maxFetchDepth);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.IJDOObjectDAO#storeJDOObject(java.lang.Object, boolean, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public ScheduledReport storeJDOObject(ScheduledReport scheduledReport,
			boolean get, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) {
		return JFireEjb3Factory.getRemoteBean(ScheduledReportManagerRemote.class, SecurityReflector.getInitialContextProperties())
			.storeScheduledReport(scheduledReport, get, fetchGroups, maxFetchDepth);
	}

	/**
	 * Deletes the {@link ScheduledReport} with the given id on the server.
	 * 
	 * @param scheduledReportID The id of the {@link ScheduledReport} to delete.
	 * @param monitor TODO
	 */
	public void deleteScheduledReport(ScheduledReportID scheduledReportID, ProgressMonitor monitor) {
		JFireEjb3Factory.getRemoteBean(ScheduledReportManagerRemote.class, SecurityReflector.getInitialContextProperties())
				.deleteScheduledReport(scheduledReportID);
	}

	/**
	 * Returns the {@link ScheduledReport} with the given {@link ScheduledReportID}
	 * detached with a minimum of the given fetch-groups.
	 *  
	 * @param scheduledReportID The id of the {@link ScheduledReport} to return.
	 * @param fetchGroups The minimum fetch-groups the returned {@link ScheduledReport} should have been detached with.
	 * @param maxFetchDepth The maximum fetch-depth the returned {@link ScheduledReport} should have been detached with.
	 * @param monitor The monitor where this operation can report progress to.
	 * @return A detached copy of the {@link ScheduledReport} with the given id, or <code>null</code> if it could not be found.
	 */
	public ScheduledReport getScheduledReport(
			ScheduledReportID scheduledReportID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, scheduledReportID, fetchGroups, maxFetchDepth, monitor);
	}
	
	/**
	 * Returns the {@link ScheduledReport}s with the given {@link ScheduledReportID}s
	 * detached with a minimum of the given fetch-groups.
	 *  
	 * @param scheduledReportIDs The ids of the {@link ScheduledReport} to return.
	 * @param fetchGroups The minimum fetch-groups the returned {@link ScheduledReport}s should have been detached with.
	 * @param maxFetchDepth The maximum fetch-depth the returned {@link ScheduledReport}s should have been detached with.
	 * @param monitor The monitor where this operation can report progress to.
	 * @return A detached copy of the {@link ScheduledReport}s with the given ids, or an empty Collection if none could be found.
	 */
	public Collection<ScheduledReport> getScheduledReports(
			Set<ScheduledReportID> scheduledReportIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, scheduledReportIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	/**
	 * Returns all {@link ScheduledReport}s of the current user
	 * detached with a minimum of the given fetch-groups.
	 *  
	 * @param fetchGroups The minimum fetch-groups the returned {@link ScheduledReport} should have been detached with.
	 * @param maxFetchDepth The maximum fetch-depth the returned {@link ScheduledReport} should have been detached with.
	 * @param monitor The monitor where this operation can report progress to.
	 * @return A detached copy of the {@link ScheduledReport} with the given id, or <code>null</code> if it could not be found.
	 */
	public Collection<ScheduledReport> getScheduledReports(
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		ScheduledReportManagerRemote srm = JFireEjb3Factory.getRemoteBean(ScheduledReportManagerRemote.class, SecurityReflector.getInitialContextProperties());
		Collection<ScheduledReportID> scheduledReportIDs = srm.getScheduledReportIDs();
		return getJDOObjects(null, scheduledReportIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	/** Static shared instance */
	private static ScheduledReportDAO sharedInstance;
	
	/**
	 * @return The (lazy created) singleton of {@link ScheduledReportDAO}.
	 */
	public static ScheduledReportDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ScheduledReportDAO.class) {
				if (sharedInstance == null) {
					sharedInstance = new ScheduledReportDAO();
				}
			}
		}
		return sharedInstance;
	}
}
