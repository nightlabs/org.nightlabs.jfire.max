package org.nightlabs.jfire.issue.issuemarker;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueMarkerID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * The companion DAO for the main IssueDAO. *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueMarkerDAO extends BaseJDOObjectDAO<IssueMarkerID, IssueMarker> {
	// :: --- [Statics] -----------------------------------------------------------------
	private static IssueMarkerDAO sharedInstance = null;
	public static IssueMarkerDAO sharedInstance() {
		if (IssueMarkerDAO.sharedInstance == null)
			synchronized(IssueMarkerDAO.class) {
				if (IssueMarkerDAO.sharedInstance == null)	IssueMarkerDAO.sharedInstance = new IssueMarkerDAO();
			}

		return IssueMarkerDAO.sharedInstance;
	}

	// :: --- [Accessors] ---------------------------------------------------------------
	@Override
	protected Collection<IssueMarker> retrieveJDOObjects(Set<IssueMarkerID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception {
		monitor.beginTask("Loading issue markers", 1);
		try {
			IssueManagerRemote cbm = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return CollectionUtil.castCollection(cbm.getIssueMarkers(objectIDs, fetchGroups, maxFetchDepth));
		}catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * Given a list of IssueMarkerIDs, retrieve the related list of IssueMarkers.
	 */
	public Collection<IssueMarker> getIssueMarkers(Set<IssueMarkerID> issueMarkerIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, issueMarkerIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all {@link IssueMarker}s belonging to a given {@link Issue} specified by its OID.
	 */
	public Collection<IssueMarker> getIssueMarkers(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Loading issue markers", 100);
		try {
			Collection<IssueMarkerID> issueMarkerIDs;
			try {
				IssueManagerRemote cbm = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
				issueMarkerIDs = CollectionUtil.castCollection(cbm.getIssueMarkerIDs(issueID));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			monitor.worked(50);

			return getJDOObjects(null, issueMarkerIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} finally {
			monitor.done();
		}
	}
}
