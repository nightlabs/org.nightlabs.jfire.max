package org.nightlabs.jfire.issue.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class IssueLinkTypeDAO
extends BaseJDOObjectDAO<IssueLinkTypeID, IssueLinkType>
{
	private IssueLinkTypeDAO() {}

	private static IssueLinkTypeDAO sharedInstance = null;

	public static IssueLinkTypeDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssueLinkTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueLinkTypeDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected Collection<IssueLinkType> retrieveJDOObjects(Set<IssueLinkTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception 
	{
		monitor.beginTask("Fetching IssueLinkType...", 1); //$NON-NLS-1$
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			return im.getIssueLinkTypes(objectIDs, fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.done();
		}
	}

	private static final String[] FETCH_GROUPS = { IssueLinkType.FETCH_GROUP_THIS_ISSUE_LINK_TYPE, FetchPlan.DEFAULT };

	public synchronized IssueLinkType getIssueLinkType(IssueLinkTypeID issueLinkTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issueLinkType "+issueLinkTypeID.issueLinkTypeID, 1);
		IssueLinkType issueLinkType = getJDOObject(null, issueLinkTypeID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueLinkType;
	}
	
	public List<IssueLinkType> getIssueLinkTypes(ProgressMonitor monitor) 
	{
		try {
			return new ArrayList<IssueLinkType>(retrieveJDOObjects(null, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
		} catch (Exception e) {
			throw new RuntimeException("Error while fetching issue link types: " + e.getMessage(), e); //$NON-NLS-1$
		} 
	}

	/**
	 * Get issue link types by link class name.
	 * @param linkedClass 
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action.
	 * 
	 * @return The issue link types of the given linkClass.
	 */
	public synchronized List<IssueLinkType> getIssueLinkTypesByLinkClass(Class<? extends Object> linkedClass, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<IssueLinkTypeID> ids = im.getIssueLinkTypesByLinkedObjectClass(linkedClass);
			return getJDOObjects(null, ids, fetchgroups, maxFetchDepth, monitor);
		} catch(Exception e) {
			throw new RuntimeException("Getting issue link types failed", e);
		}
	}
//	public IssueLinkType storeIssueLinkType(IssueLinkType issueLinkType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		if (issueLinkType == null)
//			throw new NullPointerException("Issue to save must not be null");
//		monitor.beginTask("Storing issueLinkType: "+ issueLinkType.getIssueLinkTypeID(), 3);
//		try {
//			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//			IssueLinkType result = im.storeIssueLinkType(issueLinkType, get, fetchGroups, maxFetchDepth);
//			monitor.worked(1);
//			monitor.done();
//			return result;
//		} catch (Exception e) {
//			monitor.done();
//			throw new RuntimeException("Error while storing IssueLinkType!\n" ,e);
//		}
//	}
}