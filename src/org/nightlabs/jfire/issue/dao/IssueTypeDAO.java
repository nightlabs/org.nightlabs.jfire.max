package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * 
 * @author chairatk
 *
 */
public class IssueTypeDAO extends BaseJDOObjectDAO<IssueTypeID, IssueType>
{
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssueType> retrieveJDOObjects(Set<IssueTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Fetching IssueSeverityTypes...", 1); //$NON-NLS-1$
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			
			return im.getIssueTypes(objectIDs, fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}
	
	private static IssueTypeDAO sharedInstance = null;

	/**
	 * Returns the shared instance of IssueTypeDAO.
	 **/
	public static IssueTypeDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssueTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueTypeDAO();
			}
		}
		return sharedInstance;
	}
	
	/**
	 * Get a single issue type.
	 * @param issueTypeID The ID of the issue type to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested issue object
	 */
	public synchronized IssueType getIssueType(IssueTypeID issueTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue type " + issueTypeID.issueTypeID, 1);
		IssueType issueType = getJDOObject(null, issueTypeID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueType;
	}
	
	public synchronized List<IssueType> getIssueTypes(Set<IssueTypeID> issueTypeIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, issueTypeIDs, fetchgroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all issue types.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issue types.
	 */
	public synchronized Collection<IssueType> getIssueTypes(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.getIssueTypes(fetchgroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Store an issue type.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issue type.
	 */
	public synchronized IssueType storeIssueTypes(IssueType issueType, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.storeIssueType(issueType, true, fetchgroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
