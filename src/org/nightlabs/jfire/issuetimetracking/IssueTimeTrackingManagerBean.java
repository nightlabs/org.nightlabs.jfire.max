package org.nightlabs.jfire.issuetimetracking;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;

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
public class IssueTimeTrackingManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueTimeTrackingManagerBean.class);
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	@Override
	public void setSessionContext(SessionContext sessionContext) throws EJBException,
			RemoteException {
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	
	//ProjectCost//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
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
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ProjectCost createProjectCost(Project project, Currency currency, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ProjectCost projectCost = new ProjectCost(project, currency);
			projectCost = pm.makePersistent(projectCost);
			return projectCost; 
		}//try
		finally {
			pm.close();
		}//finally
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
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
	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
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