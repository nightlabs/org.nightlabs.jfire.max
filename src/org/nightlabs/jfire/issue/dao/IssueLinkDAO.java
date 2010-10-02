package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssueLink}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueLinkDAO
extends BaseJDOObjectDAO<IssueLinkID, IssueLink>
{
	private static IssueLinkDAO sharedInstance = null;

	public static IssueLinkDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueLinkDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueLinkDAO();
			}
		}
		return sharedInstance;
	}

	private IssueManagerRemote issueManagerRemote = null;

	@Override
	protected Collection<IssueLink> retrieveJDOObjects(
			Set<IssueLinkID> issueLinkIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	throws Exception
	{
		monitor.beginTask("Loading issue links", 1);
		try {
			IssueManagerRemote im = issueManagerRemote;
			if (im == null)
				im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);

			return im.getIssueLinks(issueLinkIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public IssueLink getIssueLink(IssueLinkID issueLinkID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, issueLinkID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<IssueLink> getIssueLinks(Collection<IssueLinkID> issueLinkIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, issueLinkIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * @deprecated Use {@link #getIssueLinkIDs(ObjectID, ProgressMonitor)} or {@link #getIssueLinks(ObjectID, String[], int, ProgressMonitor)} instead!
	 */
	@Deprecated
	public synchronized Collection<IssueLink> getIssueLinksByOrganisationIDAndLinkedObjectID(String organisationID, ObjectID linkedObjectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		issueManagerRemote = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
		try {
			Collection<IssueLinkID> issueLinkIDs = issueManagerRemote.getIssueLinkIDsByOrganisationIDAndLinkedObjectID(organisationID, linkedObjectID);
			Collection<IssueLink> result = getJDOObjects(null, issueLinkIDs, fetchGroups, maxFetchDepth, monitor);
			return result;
		} finally {
			issueManagerRemote = null;
		}
	}

	public synchronized Collection<IssueLink> getIssueLinks(ObjectID linkedObjectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue links", 100);
		try {
			issueManagerRemote = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			try {
				monitor.worked(5);

				Collection<IssueLinkID> issueLinkIDs = issueManagerRemote.getIssueLinkIDs(linkedObjectID);
				monitor.worked(35);

				Collection<IssueLink> result = getJDOObjects(
						null, issueLinkIDs, fetchGroups, maxFetchDepth,
						new SubProgressMonitor(monitor, 60)
				);
				return result;
			} finally {
				issueManagerRemote = null;
			}
		} finally {
			monitor.done();
		}
	}

	public synchronized Collection<IssueLinkID> getIssueLinkIDs(ObjectID linkedObjectID, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue link IDs", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueLinkIDs(linkedObjectID);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized Map<ObjectID, Long> getIssueLinkCounts(Collection<? extends ObjectID> linkedObjectIDs, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue link counts", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueLinkCounts(linkedObjectIDs);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized Collection<IssueLinkID> getIssueLinkIDsForIssueAndLinkedObjectClass(IssueID issueID, Class<?> linkedObjectClass, ProgressMonitor monitor) {
		Set<Class<?>> linkedObjectClasses = null;
		if (linkedObjectClass != null) {
			linkedObjectClasses = new HashSet<Class<?>>(1);
			linkedObjectClasses.add(Object.class);
		}
		return getIssueLinkIDsForIssueAndLinkedObjectClasses(issueID, linkedObjectClasses, monitor);
	}

	public synchronized Collection<IssueLinkID> getIssueLinkIDsForIssueAndLinkedObjectClasses(IssueID issueID, Set<Class<?>> linkedObjectClasses, ProgressMonitor monitor) {
		monitor.beginTask("Loading issue link IDs", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueLinkIDsForIssueAndLinkedObjectClasses(issueID, linkedObjectClasses);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
}
