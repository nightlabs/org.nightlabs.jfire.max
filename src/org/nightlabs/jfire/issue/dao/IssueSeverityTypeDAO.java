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
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class IssueSeverityTypeDAO
		extends BaseJDOObjectDAO<IssueSeverityTypeID, IssueSeverityType>
{
	private IssueSeverityTypeDAO() {}

	private static IssueSeverityTypeDAO sharedInstance = null;

	public static IssueSeverityTypeDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssueSeverityTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueSeverityTypeDAO();
			}
		}
		return sharedInstance;
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssueSeverityType> retrieveJDOObjects(Set<IssueSeverityTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Fetching IssueSeverityTypes...", 1); //$NON-NLS-1$
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			return im.getIssueSeverityTypes(fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private static final String[] FETCH_GROUPS = { IssueSeverityType.FETCH_GROUP_THIS, FetchPlan.DEFAULT };

	public List<IssueSeverityType> getIssueSeverityTypes(IssueTypeID issueTypeID, ProgressMonitor monitor)
	{
		try {
			return new ArrayList<IssueSeverityType>(retrieveJDOObjects(null, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching issue severity types: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

	public IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		if(issueSeverityType == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issueSeverityType: "+ issueSeverityType.getIssueSeverityTypeID(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			IssueSeverityType result = im.storeIssueSeverityType(issueSeverityType, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing IssueSeverityType!\n" ,e);
		}
	}
}
