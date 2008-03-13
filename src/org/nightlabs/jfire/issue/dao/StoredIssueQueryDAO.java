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
import org.nightlabs.jfire.issue.config.StoredIssueQuery;
import org.nightlabs.jfire.issue.id.StoredIssueQueryID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class StoredIssueQueryDAO
extends BaseJDOObjectDAO<StoredIssueQueryID, StoredIssueQuery>
{
	private StoredIssueQueryDAO() {}

	private static StoredIssueQueryDAO sharedInstance = null;

	public static StoredIssueQueryDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (StoredIssueQueryDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new StoredIssueQueryDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<StoredIssueQuery> retrieveJDOObjects(Set<StoredIssueQueryID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {
		monitor.beginTask("Fetching StoredIssueQuery...", 1); //$NON-NLS-1$
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			return im.getStoredIssueQuery(fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private static final String[] FETCH_GROUPS = { IssuePriority.FETCH_GROUP_THIS_ISSUE_PRIORITY, FetchPlan.DEFAULT };

	public List<StoredIssueQuery> getStoredIssueQueries(ProgressMonitor monitor) {
		try {
			return new ArrayList<StoredIssueQuery>(retrieveJDOObjects(null, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching stored issue query: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

	public synchronized StoredIssueQuery getStoredIssueQuery(StoredIssueQueryID storedIssueQueryID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading storedIssueQuery " + storedIssueQueryID.storedIssueQueryID, 1);
		StoredIssueQuery issuePriority = getJDOObject(null, storedIssueQueryID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issuePriority;
	}
	
	public StoredIssueQuery storeStoredIssueQuery(StoredIssueQuery storedIssueQuery, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		if(storedIssueQuery == null)
			throw new NullPointerException("StoredIssueQuery to save must not be null");
		monitor.beginTask("Storing storedIssueQuery: "+ storedIssueQuery.getName(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			StoredIssueQuery result = im.storeStoredIssueQuery(storedIssueQuery, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing StoredIssueQuery!\n" ,e);
		}
	}
	
	public void deleteStoredIssueQuery(StoredIssueQueryID id, ProgressMonitor monitor) {
		if(id == null)
			throw new NullPointerException("StoredIssueQueryID to delete must not be null");
		monitor.beginTask("Deleting storedIssueQuery: "+ id, 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			im.deleteStoredIssueQuery(id);
			monitor.worked(1);
			monitor.done();
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while deleting StoredIssueQuery!\n" ,e);
		}
	}
}