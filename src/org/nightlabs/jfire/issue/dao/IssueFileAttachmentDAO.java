package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueFileAttachment;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssueFileAttachment}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueFileAttachmentDAO extends BaseJDOObjectDAO<IssueFileAttachmentID, IssueFileAttachment>{

	private static IssueFileAttachmentDAO sharedInstance = null;

	public static IssueFileAttachmentDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueFileAttachmentDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueFileAttachmentDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<IssueFileAttachment> retrieveJDOObjects(Set<IssueFileAttachmentID> issueFileAttachmentIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading IssueFileAttachments", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getIssueFileAttachments(issueFileAttachmentIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * Get a single issue file attachment.
	 * @param issueFileAttachmentID The ID of the issueFileAttachment to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested issue object
	 */
	public synchronized IssueFileAttachment getIssueFileAttachment(IssueFileAttachmentID issueFileAttachmentID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue "+issueFileAttachmentID.issueFileAttachmentID, 1);
		IssueFileAttachment issueFileAttachment = getJDOObject(null, issueFileAttachmentID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueFileAttachment;
	}

	public synchronized List<IssueFileAttachment> getIssueFileAttachments(Set<IssueFileAttachmentID> issueFileAttachmentIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, issueFileAttachmentIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all issues.
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issueFileAttachments.
	 */
	public synchronized Collection<IssueFileAttachment> getIssueFileAttachments(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			Set<IssueFileAttachmentID> is = im.getIssueFileAttachmentIDs();
			return getJDOObjects(null, is, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
