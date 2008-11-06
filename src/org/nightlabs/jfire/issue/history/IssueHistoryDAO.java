package org.nightlabs.jfire.issue.history;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
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

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<IssueHistory> retrieveJDOObjects(Set<IssueHistoryID> issueHistoryIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issue Histories", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.getIssueHistories(issueHistoryIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
	
	private IssueManager issueManager;

	@SuppressWarnings("unchecked")
	public synchronized List<IssueHistory> getIssueHistories(IssueID issueID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			issueManager = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
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
	
	@SuppressWarnings("unchecked")
	public Collection<IssueHistory> getIssueHistories(Collection<IssueHistoryID> issueHistoryIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue histories...", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<IssueHistory> issueHistories = im.getIssueHistories(issueHistoryIDs, fetchGroups, maxFetchDepth);
			monitor.done();
			return issueHistories;			
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
