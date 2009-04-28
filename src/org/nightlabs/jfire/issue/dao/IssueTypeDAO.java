package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link IssueType}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueTypeDAO extends BaseJDOObjectDAO<IssueTypeID, IssueType>
{
	/**
	 * The shared instance
	 */

	private static IssueTypeDAO sharedInstance = null;

	/**
	 * Default constructor.
	 */
	public IssueTypeDAO() {
		super();
	}

	/**
	 * Get the lazily created shared instance.
	 * @return The shared instance
	 */
	public static IssueTypeDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (IssueTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueTypeDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected Collection<IssueType> retrieveJDOObjects(Set<IssueTypeID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Fetching "+objectIDs.size()+" issue types information", 1);
		Collection<IssueType> issueTypes;
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			issueTypes = im.getIssueTypes(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Failed downloading issue types information!", e);
		}
		monitor.done();
		return issueTypes;
	}

	/**
	 * Get a single issue type.
	 * @param issueTypeID The ID of the issue type to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested issue object
	 */
	public IssueType getIssueType(IssueTypeID issueTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issueType " + issueTypeID.issueTypeID, 1);
		IssueType issueType = getJDOObject(null, issueTypeID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueType;
	}

	public List<IssueType> getIssueTypes(Set<IssueTypeID> issueTypeIDs, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issueTypes ", 1);
		List<IssueType> issueTypes = getJDOObjects(null, issueTypeIDs, fetchgroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return issueTypes;
	}

	public List<IssueType> getAllIssueTypes(String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Fetching all IssueTypes", 100);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(20);
			Set<IssueTypeID> allTypeIDs = im.getIssueTypeIDs();
			List<IssueType> allTypes = getIssueTypes(allTypeIDs, fetchgroups, maxFetchDepth, new SubProgressMonitor(monitor, 80));
			monitor.done();
			return allTypes;
		}
		catch (Exception e) {
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;

			throw new RuntimeException(e);
		}
	}

	/**
	 * Store an issue type.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The issue type.
	 */
	public synchronized IssueType storeIssueTypes(IssueType issueType, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.storeIssueType(issueType, true, fetchgroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
