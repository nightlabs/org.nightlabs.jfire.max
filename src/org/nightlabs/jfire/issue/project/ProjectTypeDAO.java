package org.nightlabs.jfire.issue.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link ProjectType}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class ProjectTypeDAO
extends BaseJDOObjectDAO<ProjectTypeID, ProjectType>
{
	private static ProjectTypeDAO sharedInstance = null;

	public static ProjectTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (ProjectTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProjectTypeDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<ProjectType> retrieveJDOObjects(Set<ProjectTypeID> projectTypeIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading ProjectTypes", 1);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return im.getProjectTypes(projectTypeIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	private IssueManagerRemote issueManager;

	/**
	 * Get a single projectType.
	 * @param projectTypeID The ID of the projectType to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested projectType object
	 */
	public synchronized ProjectType getProjectType(ProjectTypeID projectTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading projectType " + projectTypeID.projectTypeID, 1);
		ProjectType projectType = getJDOObject(null, projectTypeID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return projectType;
	}

	public synchronized List<ProjectType> getProjectTypes(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			issueManager = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			try {
				Collection<ProjectTypeID> projectTypeIDs = issueManager.getProjectTypeIDs();
				return getJDOObjects(null, projectTypeIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				issueManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public Collection<ProjectType> getProjectTypes(Collection<ProjectTypeID> projectTypeIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, projectTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized ProjectType storeProjectType(ProjectType projectType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(projectType == null)
			throw new NullPointerException("ProjectType to save must not be null");
		monitor.beginTask("Storing projectType: "+ projectType.getProjectTypeID(), 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			monitor.worked(1);

			ProjectType result = im.storeProjectType(projectType, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}

	public synchronized void deleteProjectType(ProjectTypeID projectTypeID, ProgressMonitor monitor) {
		monitor.beginTask("Deleting projectType: "+ projectTypeID, 3);
		try {
			IssueManagerRemote im = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			im.deleteProjectType(projectTypeID);
			monitor.worked(1);
			monitor.done();
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while deleting projectType!\n" ,e);
		}
	}
}
