package org.nightlabs.jfire.issue.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueStatus;
import org.nightlabs.jfire.issue.id.IssueStatusID;
import org.nightlabs.progress.ProgressMonitor;

public class IssueStatusDAO
		extends BaseJDOObjectDAO<IssueStatusID, IssueStatus>
{
	private IssueStatusDAO() {}

	private static IssueStatusDAO sharedInstance = null;

	public static IssueStatusDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssueStatusDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueStatusDAO();
			}
		}
		return sharedInstance;
	}
//	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssueStatus> retrieveJDOObjects(Set<IssueStatusID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Fetching IssueStatus...", 1); //$NON-NLS-1$
		try {
//			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			return null;//im.getIssueStatus(fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private static final String[] FETCH_GROUPS = { IssueStatus.FETCH_GROUP_THIS, FetchPlan.DEFAULT };

	public List<IssueStatus> getIssueStatus(ProgressMonitor monitor)
	{
		try {
			return new ArrayList<IssueStatus>(retrieveJDOObjects(null, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching issue status: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

}
