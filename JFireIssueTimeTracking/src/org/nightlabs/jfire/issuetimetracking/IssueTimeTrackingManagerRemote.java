package org.nightlabs.jfire.issuetimetracking;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;

@Remote
public interface IssueTimeTrackingManagerRemote
{
	@RolesAllowed("_Guest_")
	List<ProjectCost> getProjectCosts(Collection<ProjectCostID> projectCostIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores a project cost to the datastore.
	 * @param projectCost the project cost to store
	 * @param get true if you want to get the stored project type
	 * @param fetchGroups the fetchGroups that used for specify fields to be detached from the datastore
	 * @param maxFetchDepth specifies the number of level of the object to be fetched
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	ProjectCost storeProjectCost(ProjectCost projectCost, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	@RolesAllowed("_Guest_")
	Set<ProjectCostID> getProjectCostIDsByProjectID(ProjectID projectID);

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;

}