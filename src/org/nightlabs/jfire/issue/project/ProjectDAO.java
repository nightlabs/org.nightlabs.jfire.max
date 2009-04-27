package org.nightlabs.jfire.issue.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link Project}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class ProjectDAO extends BaseJDOObjectDAO<ProjectID, Project>{

	private static ProjectDAO sharedInstance = null;

	public static ProjectDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (ProjectDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProjectDAO();
			}
		}
		return sharedInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected synchronized Collection<Project> retrieveJDOObjects(Set<ProjectID> projectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Projects", 1);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return im.getProjects(projectIDs, fetchGroups, maxFetchDepth);
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
	 * Get a single project.
	 * @param projectID The ID of the project to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested project object
	 */
	public synchronized Project getProject(ProjectID projectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading project " + projectID.projectID, 1);
		Project project = getJDOObject(null, projectID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return project;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<Project> getProjects(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			{
		try {
			issueManager = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProjectID> projectIDs = issueManager.getProjectIDs();
				return getJDOObjects(null, projectIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				issueManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
			}

	@SuppressWarnings("unchecked")
	public Collection<Project> getProjects(Collection<ProjectID> projectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, projectIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized Project storeProject(Project project, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(project == null)
			throw new NullPointerException("Project to save must not be null");
		monitor.beginTask("Storing project: "+ project.getProjectID(), 3);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			if (project.getProjectType() == null) {
				project.setProjectType(
						ProjectTypeDAO.sharedInstance().getProjectType(
								ProjectType.PROJECT_TYPE_ID_DEFAULT,
								new String[] {FetchPlan.DEFAULT},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new NullProgressMonitor()
						)
				);
			}

			Project result = im.storeProject(project, get, fetchGroups, maxFetchDepth);
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

	public synchronized void deleteProject(ProjectID projectID, ProgressMonitor monitor) {
		monitor.beginTask("Deleting project: "+ projectID, 3);
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			im.deleteProject(projectID);
			monitor.worked(1);
			monitor.done();
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while deleting project!\n" ,e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<Project> getRootProjects(String organisationID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(organisationID == null)
			throw new NullPointerException("OrganisationID must not be null");
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<Project> result = getProjects(im.getRootProjectIDs(organisationID), fetchGroups, maxFetchDepth, monitor);
			result.add(getProject(Project.PROJECT_ID_DEFAULT, fetchGroups, maxFetchDepth, monitor));
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<Project> getProjectsByParentProjectID(ProjectID projectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<Project> result = ProjectDAO.sharedInstance.getProjects(im.getProjectIDsByParentProjectID(projectID), fetchGroups, maxFetchDepth, monitor);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<Project> getProjectsByProjectTypeID(ProjectTypeID projectTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			IssueManagerRemote im = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Collection<Project> result = ProjectDAO.sharedInstance.getProjects(im.getProjectIDsByProjectTypeID(projectTypeID), fetchGroups, maxFetchDepth, monitor);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
