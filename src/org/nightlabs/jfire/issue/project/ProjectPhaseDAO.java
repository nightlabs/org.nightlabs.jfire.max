package org.nightlabs.jfire.issue.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.project.id.ProjectPhaseID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link ProjectPhase}s.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class ProjectPhaseDAO extends BaseJDOObjectDAO<ProjectPhaseID, ProjectPhase>{

	private static ProjectPhaseDAO sharedInstance = null;

	public static ProjectPhaseDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (ProjectPhaseDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProjectPhaseDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected synchronized Collection<ProjectPhase> retrieveJDOObjects(Set<ProjectPhaseID> projectPhaseIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Project Phases", 1);
		try {
			IssueManager im = JFireEjbUtil.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			return im.getProjectPhases(projectPhaseIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	private IssueManager issueManager;

	/**
	 * Get a single project.
	 * @param projectPhaseID The ID of the project phase to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested project phase object
	 */
	public synchronized ProjectPhase getProjectPhase(ProjectPhaseID projectPhaseID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading project phase" + projectPhaseID.projectPhaseID, 1);
		ProjectPhase projectPhase = getJDOObject(null, projectPhaseID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		monitor.done();
		return projectPhase;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<ProjectPhase> getProjectPhases(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			{
		try {
			issueManager = JFireEjbUtil.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProjectPhaseID> projectPhaseIDs = issueManager.getProjectPhaseIDs();
				return getJDOObjects(null, projectPhaseIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				issueManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
			}

	@SuppressWarnings("unchecked")
	public Collection<ProjectPhase> getProjectPhases(Collection<ProjectPhaseID> projectPhaseIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, projectPhaseIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized ProjectPhase storeProjectPhase(ProjectPhase projectPhase, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(projectPhase == null)
			throw new NullPointerException("Project Phase to save must not be null");
		monitor.beginTask("Storing project phase : "+ projectPhase.getProjectPhaseID(), 3);
		try {
			IssueManager im = JFireEjbUtil.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			ProjectPhase result = im.storeProjectPhase(projectPhase, get, fetchGroups, maxFetchDepth);
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
