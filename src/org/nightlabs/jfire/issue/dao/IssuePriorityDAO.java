package org.nightlabs.jfire.issue.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

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
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssuePriority> retrieveJDOObjects(Set<IssuePriorityID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Fetching IssuePriority...", 1); //$NON-NLS-1$
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			return im.getIssuePriorities(fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private static final String[] FETCH_GROUPS = { IssuePriority.FETCH_GROUP_THIS, FetchPlan.DEFAULT };

	public List<IssuePriority> getIssuePriorities(ProgressMonitor monitor)
	{
		try {
			return new ArrayList<IssuePriority>(retrieveJDOObjects(null, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching issue status: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

}