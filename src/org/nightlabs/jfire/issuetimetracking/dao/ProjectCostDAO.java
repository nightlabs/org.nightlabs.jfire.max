package org.nightlabs.jfire.issuetimetracking.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.dao.IssueDAO;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issuetimetracking.IssueTimeTrackingManagerRemote;
import org.nightlabs.jfire.issuetimetracking.ProjectCost;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class ProjectCostDAO
extends BaseJDOObjectDAO<ProjectCostID, ProjectCost>
{
	private static ProjectCostDAO sharedInstance = null;
	public static ProjectCostDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProjectCostDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<ProjectCost> retrieveJDOObjects(
			Set<ProjectCostID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Loading Project Costs", 1);
		try {
			IssueTimeTrackingManagerRemote it = JFireEjb3Factory.getRemoteBean(IssueTimeTrackingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return it.getProjectCosts(objectIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized ProjectCost getProjectCost(ProjectID projectID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			IssueTimeTrackingManagerRemote issueTimeTrackingManager = JFireEjb3Factory.getRemoteBean(IssueTimeTrackingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<ProjectCostID> projectCostIDs = issueTimeTrackingManager.getProjectCostIDsByProjectID(projectID);
				List<ProjectCost> projectCosts = getJDOObjects(null, projectCostIDs, fetchGroups, maxFetchDepth, monitor);
				return projectCosts.isEmpty() ? null : projectCosts.get(0);
			} finally {
				issueTimeTrackingManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

//	public synchronized ProjectCost createProjectCost(
//			Project project, Currency currency, boolean get, String[] fetchGroups,
//			int maxFetchDepth, ProgressMonitor monitor){
//		if(project == null)
//			throw new NullPointerException("Project must not be null");
//		if(currency == null)
//			throw new NullPointerException("Currency must not be null");
//		monitor.beginTask("Creating project costs for project: "+ project.getProjectID(), 3);
//		try {
//			IssueTimeTrackingManager it = JFireEjbFactory.getBean(IssueTimeTrackingManager.class, SecurityReflector.getInitialContextProperties());
//			monitor.worked(1);
//
//			ProjectCost result = it.storeProjectCost(projectCost, get, fetchGroups, maxFetchDepth)(project, currency, get, fetchGroups, maxFetchDepth);
//			if (result != null)
//				getCache().put(null, result, fetchGroups, maxFetchDepth);
//
//			monitor.worked(1);
//			monitor.done();
//			return result;
//		} catch (Exception e) {
//			monitor.done();
//			throw new RuntimeException(e);
//		}
//	}

	/**
	 * Store a project cost.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The project cost.
	 */
	public synchronized ProjectCost storeProjectCost(ProjectCost projectCost, String[] fetchgroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(projectCost.getProject() == null)
			throw new NullPointerException("Project must not be null");
		if(projectCost.getCurrency() == null)
			throw new NullPointerException("Currency must not be null");
		monitor.beginTask("Creating project costs for project: "+ projectCost.getProjectID(), 3);
		try {
			IssueTimeTrackingManagerRemote it = JFireEjb3Factory.getRemoteBean(IssueTimeTrackingManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			ProjectCost result = it.storeProjectCost(projectCost, true, fetchgroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchgroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
