package org.nightlabs.jfire.issue.history;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.history.id.IssueHistoryItemID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data access object for {@link IssueHistoryItem}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueHistoryItemDAO extends BaseJDOObjectDAO<IssueHistoryItemID, IssueHistoryItem>{

	private static IssueHistoryItemDAO sharedInstance = null;

	public static IssueHistoryItemDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueHistoryItemDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueHistoryItemDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<IssueHistoryItem> retrieveJDOObjects(Set<IssueHistoryItemID> issueHistoryIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issue Histories", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getIssueHistoryItems(issueHistoryIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	private IssueManagerRemote issueManager;

	public synchronized List<IssueHistoryItem> getIssueHistoryItems
	(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			issueManager = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<IssueHistoryItemID> issueHistoryItemIDs = issueManager.getIssueHistoryItemIDsByIssueID(issueID);
				return getJDOObjects(null, issueHistoryItemIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				issueManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public Collection<IssueHistoryItem> getIssueHistoryItems
	(Collection<IssueHistoryItemID> issueHistoryItemIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Loading issue histories...", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<IssueHistoryItem> issueHistoryItems = im.getIssueHistoryItems(issueHistoryItemIDs, fetchGroups, maxFetchDepth);
			monitor.done();
			return issueHistoryItems;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
