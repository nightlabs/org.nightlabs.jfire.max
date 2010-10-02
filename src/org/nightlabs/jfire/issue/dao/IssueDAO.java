package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.query.IssueQuery;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link Issue}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueDAO extends BaseJDOObjectDAO<IssueID, Issue>{

	private static IssueDAO sharedInstance = null;

	public static IssueDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<Issue> retrieveJDOObjects(Set<IssueID> issueIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issues", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssues(issueIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized Issue storeIssue(Issue issue, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(issue == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issue: "+ issue.getIssueID(), 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			monitor.worked(1);

			Issue result = im.storeIssue(issue, jbpmTransitionName, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}

	public synchronized Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(issue == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issue: "+ issue.getIssueID(), 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			monitor.worked(1);

			Issue result = im.storeIssue(issue, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}

	public synchronized void deleteIssue(IssueID issueID, ProgressMonitor monitor) {
		monitor.beginTask("Deleting issue: "+ issueID, 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			im.deleteIssue(issueID);
			monitor.worked(1);
			monitor.done();
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while deleting Issue!\n" ,e);
		}
	}

	/**
	 * Get a single issue.
	 * @param issueID The ID of the issue to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested issue object
	 */
	public synchronized Issue getIssue(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue "+issueID.issueID, 1);
		Issue issue = getJDOObject(null, issueID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issue;
	}

	public synchronized List<Issue> getIssues(Set<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, issueIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all issues.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issues.
	 */
	public synchronized Collection<Issue> getIssues(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issues", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			Set<IssueID> is = im.getIssueIDs();
			monitor.done();
			return getJDOObjects(null, is, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Issue> getIssuesForQueries(QueryCollection<? extends IssueQuery> queries,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			Set<IssueID> issueIDs = im.getIssueIDs(queries);
			return getJDOObjects(null, issueIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public synchronized Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(issueID == null)
			throw new NullPointerException("issueID must not be null");

		monitor.beginTask("Performing transition for issue "+ issueID, 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			monitor.worked(1);

			Issue result = im.signalIssue(issueID, jbpmTransitionName, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}
}
