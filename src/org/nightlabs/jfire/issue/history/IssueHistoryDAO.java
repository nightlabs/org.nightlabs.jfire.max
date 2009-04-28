package org.nightlabs.jfire.issue.history;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.history.id.IssueHistoryID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data access object for {@link IssueHistory}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueHistoryDAO extends BaseJDOObjectDAO<IssueHistoryID, IssueHistory>{

	private static IssueHistoryDAO sharedInstance = null;

	public static IssueHistoryDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueHistoryDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueHistoryDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<IssueHistory> retrieveJDOObjects(Set<IssueHistoryID> issueHistoryIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issue Histories", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getIssueHistories(issueHistoryIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	private IssueManagerRemote issueManager;

	public synchronized List<IssueHistory> getIssueHistories(IssueID issueID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			issueManager = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<IssueHistoryID> issueHistoryIDs = issueManager.getIssueHistoryIDsByIssueID(issueID);
				return getJDOObjects(null, issueHistoryIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				issueManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public Collection<IssueHistory> getIssueHistories(Collection<IssueHistoryID> issueHistoryIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue histories...", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<IssueHistory> issueHistories = im.getIssueHistories(issueHistoryIDs, fetchGroups, maxFetchDepth);
			monitor.done();
			return issueHistories;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
