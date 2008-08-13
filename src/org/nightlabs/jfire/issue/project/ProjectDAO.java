package org.nightlabs.jfire.issue.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

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

	@Override
	protected synchronized Collection<Project> retrieveJDOObjects(Set<ProjectID> projectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Projects", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return im.getProjects(projectIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
	
	private IssueManager issueManager;

	public synchronized List<Project> getProjects(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			issueManager = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
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
		monitor.beginTask("Loading projects...", 1);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<Project> projects = im.getProjects(projectIDs, fetchGroups, maxFetchDepth);
			monitor.done();
			return projects;			
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	public synchronized Project storeProject(Project project, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(project == null)
			throw new NullPointerException("Project to save must not be null");
		monitor.beginTask("Storing project: "+ project.getProjectID(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);

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
}
