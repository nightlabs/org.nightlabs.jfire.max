package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

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

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected Collection<IssueComment> retrieveJDOObjects(Set<IssueCommentID> commentIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Loading IssueComments", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getIssueComments(commentIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized IssueComment storeIssueComment(IssueComment issueComment, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.storeIssueComment(issueComment, true, fetchgroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
