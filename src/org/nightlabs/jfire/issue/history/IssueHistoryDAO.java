package org.nightlabs.jfire.issue.history;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.history.id.IssueHistoryID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

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
	protected synchronized Collection<IssueHistory> retrieveJDOObjects(Set<IssueHistoryID> issueHistoryIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issues", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.getIssues(issueHistoryIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<IssueHistory> getIssueHistoryByIssue(Issue issue, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue histories"+ issue.getIssueID(), 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<IssueHistory> issueHistories = im.getIssueHistoryByIssue(issue);
			monitor.done();
			return issueHistories;			
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
