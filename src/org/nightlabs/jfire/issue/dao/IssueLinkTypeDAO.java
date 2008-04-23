package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	protected Collection<IssueLinkType> retrieveJDOObjects(
			Set<IssueLinkTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	throws Exception 
	{
		monitor.beginTask("Fetching IssueLinkType...", 1); //$NON-NLS-1$
		try {
			IssueManager im = issueManager;
			if (im == null)
				im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			return im.getIssueLinkTypes(objectIDs, fetchGroups, maxFetchDepth);	
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public IssueLinkType getIssueLinkType(IssueLinkTypeID issueLinkTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issueLinkType "+issueLinkTypeID.issueLinkTypeID, 1);
		IssueLinkType issueLinkType = getJDOObject(null, issueLinkTypeID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueLinkType;
	}

	private IssueManager issueManager;

	public synchronized List<IssueLinkType> getIssueLinkTypes(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			issueManager = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<IssueLinkTypeID> issueLinkTypeIDs = issueManager.getIssueLinkTypeIDs(null);
			return getJDOObjects(null, issueLinkTypeIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			issueManager = null;
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
	public synchronized Collection<IssueLinkType> getIssueLinkTypes(Class<?> linkedObjectClass, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue link types", 100);
		try {
			issueManager = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<IssueLinkTypeID> issueLinkTypeIDs = issueManager.getIssueLinkTypeIDs(linkedObjectClass);
			monitor.worked(30);
			return getJDOObjects(null, issueLinkTypeIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 70));
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			issueManager = null;
		}
	}

//	public IssueLinkType storeIssueLinkType(IssueLinkType issueLinkType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//	if (issueLinkType == null)
//	throw new NullPointerException("Issue to save must not be null");
//	monitor.beginTask("Storing issueLinkType: "+ issueLinkType.getIssueLinkTypeID(), 3);
//	try {
//	IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
//	IssueLinkType result = im.storeIssueLinkType(issueLinkType, get, fetchGroups, maxFetchDepth);
//	monitor.worked(1);
//	monitor.done();
//	return result;
//	} catch (Exception e) {
//	monitor.done();
//	throw new RuntimeException("Error while storing IssueLinkType!\n" ,e);
//	}
//	}
}