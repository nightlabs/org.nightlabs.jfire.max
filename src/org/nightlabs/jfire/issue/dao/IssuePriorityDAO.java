package org.nightlabs.jfire.issue.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssuePriority}s.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssuePriorityDAO
extends BaseJDOObjectDAO<IssuePriorityID, IssuePriority>
{
	private IssuePriorityDAO() {}

	private static IssuePriorityDAO sharedInstance = null;

	public static IssuePriorityDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssuePriorityDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssuePriorityDAO();
			}
		}
		return sharedInstance;
	}

	private IssueManager issueManager;

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssuePriority> retrieveJDOObjects(Set<IssuePriorityID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception 
	{
		monitor.beginTask("Fetching IssuePriority...", 1); //$NON-NLS-1$
		try {
			IssueManager im = issueManager;
			if (im == null)
				im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			monitor.worked(1);
			return im.getIssuePriorities(objectIDs, fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	public IssuePriority getIssuePriority(IssuePriorityID issuePriorityID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issuePriority "+issuePriorityID.issuePriorityID, 1);
		IssuePriority issuePriority = getJDOObject(null, issuePriorityID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issuePriority;
	}
	
	public synchronized List<IssuePriority> getIssuePriorities(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			return new ArrayList<IssuePriority>(retrieveJDOObjects(null, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching issue resolutions: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

	public IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (issuePriority == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issuePriority: "+ issuePriority.getIssuePriorityID(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			IssuePriority result = im.storeIssuePriority(issuePriority, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing IssuePriority!\n" ,e);
		}
	}
}