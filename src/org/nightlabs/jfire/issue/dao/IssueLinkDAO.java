package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

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

	@Override
	protected Collection<IssueLink> retrieveJDOObjects(Set<IssueLinkID> issueLinkIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Loading IssueLinks...", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
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
		try {
			return getJDOObject(null, issueLinkID, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized Collection<IssueLink> getIssueLinksByOrganisationIDAndLinkedObjectID(String organisationID, ObjectID linkedObjectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<IssueLink> result =
				getJDOObjects(null, im.getIssueLinkIDsByOrganisationIDAndLinkedObjectID(organisationID, linkedObjectID), fetchGroups, maxFetchDepth, monitor);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
