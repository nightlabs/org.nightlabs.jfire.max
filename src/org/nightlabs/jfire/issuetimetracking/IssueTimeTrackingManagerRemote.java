package org.nightlabs.jfire.issuetimetracking;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;

@Remote
public interface IssueTimeTrackingManagerRemote
{

	List<ProjectCost> getProjectCosts(Collection<ProjectCostID> projectCostIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores a project cost to the datastore.
	 * @param projectCost the project cost to store
	 * @param get true if you want to get the stored project type
	 * @param fetchGroups the fetchGroups that used for specify fields to be detached from the datastore
	 * @param maxFetchDepth specifies the number of level of the object to be fetched
	 */
	ProjectCost storeProjectCost(ProjectCost projectCost, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	Set<ProjectCostID> getProjectCostIDsByProjectID(ProjectID projectID);

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 */
	void initialise() throws Exception;

}