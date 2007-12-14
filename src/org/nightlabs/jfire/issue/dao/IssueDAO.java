package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

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
	protected synchronized Collection<Issue> retrieveJDOObjects(Set<IssueID> issueIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issues", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.getIssues(issueIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

//	public synchronized IssueHistory createIssueHistory(IssueHistory issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
//		if(issueHistory == null)
//			throw new NullPointerException("IssueHistory to create must not be null");
//		monitor.beginTask("Creating issue history: "+ issueHistory.getIssueID(), 3);
//		try {
//			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
////			IssueHistory result = im.createIssueHistory(issueHistory, get, fetchGroups, maxFetchDepth);
////			monitor.worked(1);
////			monitor.done();
//			return null;//result;
//		} catch (Exception e) {
//			monitor.done();
//			throw new RuntimeException("Error while storing Issue!\n" ,e);
//		}
//	}
	
	public synchronized Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(issue == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issue: "+ issue.getIssueID(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Issue result = im.storeIssue(issue, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing Issue!\n" ,e);
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
	
	public synchronized List<Issue> getIssues(Set<IssueID> issueIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, issueIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all issues.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issues.
	 */
	public synchronized Collection<Issue> getIssues(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<IssueID> is = im.getIssueIDs();
			return getIssues(is, fetchgroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Issue> getIssuesForQueries(Collection<JDOQuery> queries, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<IssueID> issueIDs = im.getIssueIDs(queries);
			return getJDOObjects(null, issueIDs, fetchGroups, maxFetchDepth, monitor);			
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
