package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueLinkType;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssueLinkType}s.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
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
	/**
	 * {@inheritDoc}
	 */
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
				im = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());

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

	@SuppressWarnings("unchecked")
	public synchronized List<IssueLinkType> getIssueLinkTypes(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			issueManager = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			Set<IssueLinkTypeID> issueLinkTypeIDs = issueManager.getIssueLinkTypeIDs();
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
	@SuppressWarnings("unchecked")
	public synchronized Collection<IssueLinkType> getIssueLinkTypes(Class<?> linkedObjectClass, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue link types", 100);
		try {
			issueManager = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			Set<IssueLinkTypeID> issueLinkTypeIDs = issueManager.getIssueLinkTypeIDs(linkedObjectClass);
			monitor.worked(30);
			return getJDOObjects(null, issueLinkTypeIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 70));
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			issueManager = null;
		}
	}
}