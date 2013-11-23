package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssueComment}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 */
public class IssueCommentDAO extends BaseJDOObjectDAO<IssueCommentID, IssueComment> {

	private static IssueCommentDAO sharedInstance = null;

	public static IssueCommentDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueCommentDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueCommentDAO();
			}
		}
		return sharedInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<IssueComment> retrieveJDOObjects(
			Set<IssueCommentID> commentIDs,
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor
	)
	throws Exception
	{
		monitor.beginTask("Loading issue comments", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueComments(commentIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * @deprecated Odd version of this method to be removed soon!
	 */
	@Deprecated
	public synchronized IssueComment storeIssueComment(IssueComment issueComment, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return storeIssueComment(issueComment, true, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized IssueComment storeIssueComment(IssueComment issueComment, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Storing issue comment", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.storeIssueComment(issueComment, get, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public List<IssueComment> getIssueComments(Set<IssueCommentID> issueCommentIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, issueCommentIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<IssueCommentID> getIssueCommentIDs(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue comment IDs", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			List<IssueCommentID> issueCommentIDs = im.getIssueCommentIDs(issueID);
			return issueCommentIDs;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized List<IssueComment> getIssueComments(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue comments", 100);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			monitor.worked(30);
			List<IssueCommentID> issueCommentIDs = im.getIssueCommentIDs(issueID);
			return getJDOObjects(null, issueCommentIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 70));
		} finally {
			monitor.done();
		}
	}

	public synchronized List<IssueCommentID> getIssueCommentIDsOfIssueOfIssueLink(IssueLinkID issueLinkID, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue comment IDs", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueCommentIDsOfIssueOfIssueLink(issueLinkID);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public Map<IssueLinkID, Long> getIssueCommentCountsOfIssueOfIssueLinks(Collection<IssueLinkID> issueLinkIDs, ProgressMonitor monitor) {
		monitor.beginTask("Loading issue comment ID counts", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueCommentCountsOfIssueOfIssueLinks(issueLinkIDs);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
	
	public synchronized void deleteIssueComment(IssueCommentID issueCommentID, ProgressMonitor monitor) {
		monitor.beginTask("Deleting issue comment: "+ issueCommentID, 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			im.deleteIssueComment(issueCommentID);
			monitor.worked(1);
			monitor.done();
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while deleting Issue comment!\n" ,e);
		}
	}
}
