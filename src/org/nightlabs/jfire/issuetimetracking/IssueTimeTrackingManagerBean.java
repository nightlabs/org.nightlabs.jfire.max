package org.nightlabs.jfire.issuetimetracking;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.issue.project.id.ProjectID;

/**
 * An EJB session bean provides methods for managing every objects used in the issue time tracking.
 * <p>
 *
 * </p>
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @ejb.bean name="jfire/ejb/JFireIssueTimeTracking/IssueTimeTrackingManager"
 *           jndi-name="jfire/ejb/JFireIssueTimeTracking/IssueTimeTrackingManager"
 *           type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class IssueTimeTrackingManagerBean
extends BaseSessionBeanImpl
implements IssueTimeTrackingManagerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueTimeTrackingManagerBean.class);

	//ProjectCost//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public List<ProjectCost> getProjectCosts(Collection<ProjectCostID> projectCostIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectCostIDs, ProjectCost.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public ProjectCost createProjectCost(Project project, Currency currency, boolean get, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			ProjectCost projectCost = new ProjectCost(project, currency);
//			return NLJDOHelper.storeJDO(pm, projectCost, get, fetchGroups, maxFetchDepth);
//		}//try
//		finally {
//			pm.close();
//		}//finally
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issuetimetracking.IssueTimeTrackingManagerRemote#storeProjectCost(org.nightlabs.jfire.issuetimetracking.ProjectCost, boolean, java.lang.String[], int)
	 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@RolesAllowed("_Guest_")
	public ProjectCost storeProjectCost(ProjectCost projectCost, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, projectCost, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public Set<ProjectCostID> getProjectCostIDsByProjectID(ProjectID projectID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(ProjectCost.class, "getProjectCostsByProjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectID.organisationID);
			params.put("projectID", projectID.projectID);
			return NLJDOHelper.getObjectIDSet((Collection<ProjectCostID>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	//Bean//
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issuetimetracking.IssueTimeTrackingManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			IssueTimeTrackingStruct.getIssueTimeTrackingStruct(pm);
		} finally {
			pm.close();
		}

	}
}