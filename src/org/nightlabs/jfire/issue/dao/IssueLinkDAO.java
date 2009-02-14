package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssueManager;
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
			IssueManager im = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			return im.getIssueLinks(issueLinkIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Collection<IssueLink> getIssueLinksByOrganisationIDAndLinkedObjectID(String organisationID, String linkedObjectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManager im = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			Collection<IssueLink> result = 
				getJDOObjects(null, im.getIssueLinkIDsByOrganisationIDAndLinkedObjectID(organisationID, linkedObjectID), fetchGroups, maxFetchDepth, monitor);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
