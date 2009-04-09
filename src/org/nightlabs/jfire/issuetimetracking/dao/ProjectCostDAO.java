package org.nightlabs.jfire.issuetimetracking.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issuetimetracking.ProjectCost;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;
import org.nightlabs.progress.ProgressMonitor;

public class ProjectCostDAO  extends BaseJDOObjectDAO<ProjectCostID, ProjectCost>{

	@Override
	protected Collection<ProjectCost> retrieveJDOObjects(
			Set<ProjectCostID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		return null;
	}

}
