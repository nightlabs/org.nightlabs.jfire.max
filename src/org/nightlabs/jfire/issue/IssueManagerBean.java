package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.IssueHistory;
import org.nightlabs.jfire.issue.history.id.IssueHistoryID;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.jbpm.JbpmConstants;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.ProjectPhase;
import org.nightlabs.jfire.issue.project.ProjectType;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issue.project.id.ProjectPhaseID;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.issue.prop.IssueStruct;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
// @Chairat: JFireIssueTracking MUST NOT be dependent on JFireTrade!!! Why did you add these imports, that aren't even necessary? They break the nightly build. Marco.
//import org.nightlabs.jfire.trade.Order;
//import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public class IssueManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}

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

	//IssueFileAttachment//
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueFileAttachmentID> getIssueFileAttachmentIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueFileAttachment.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueFileAttachmentID>((Collection<? extends IssueFileAttachmentID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueFileAttachment> getIssueFileAttachments(Collection<IssueFileAttachmentID> issueFileAttachmentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueFileAttachmentIDs, IssueFileAttachment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueWorkTimeRange//
	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueWorkTimeRange> getIssueWorkTimeRanges(Collection<IssueWorkTimeRange> issueWorkTimeRangeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueWorkTimeRangeIDs, IssueWorkTimeRange.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//ProjectType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ProjectType storeProjectType(ProjectType projectType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, projectType, get, fetchGroups, maxFetchDepth);
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
	public void deleteProjectType(ProjectTypeID projectTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(ProjectType.class, true);
			ProjectType projectType = (ProjectType) pm.getObjectById(projectTypeID);
			pm.deletePersistent(projectType);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<ProjectType> getProjectTypes(Collection<ProjectTypeID> projectTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectTypeIDs, ProjectType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProjectTypeID> getProjectTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(ProjectType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<ProjectTypeID>((Collection<? extends ProjectTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	//Project//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Project storeProject(Project project, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			boolean isNew = !JDOHelper.isDetached(project);
			if (!isNew) {
				project.setUpdateTimestamp(new Date());
			}
			return NLJDOHelper.storeJDO(pm, project, get, fetchGroups, maxFetchDepth);
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
	public void deleteProject(ProjectID projectID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(Project.class, true);
			Project project = (Project) pm.getObjectById(projectID);
			pm.deletePersistent(project);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Project> getProjects(Collection<ProjectID> projectIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectIDs, Project.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProjectID> getProjectIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Project.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<ProjectID>((Collection<? extends ProjectID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ProjectID> getRootProjectIDs(String organisationID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getRootProjects");
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put("organisationID", organisationID);
			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ProjectID> getProjectIDsByParentProjectID(ProjectID projectID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getProjectsByParentProjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectID.organisationID);
			params.put("parentProjectID", projectID.projectID);
			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ProjectID> getProjectIDsByProjectTypeID(ProjectTypeID projectTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getProjectsByProjectTypeID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectTypeID.organisationID);
			params.put("projectTypeID", projectTypeID.projectTypeID);
			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	//ProjectPhase//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ProjectPhase storeProjectPhase(ProjectPhase projectPhase, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
//			boolean isNew = !JDOHelper.isDetached(projectPhase);
			return NLJDOHelper.storeJDO(pm, projectPhase, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProjectPhaseID> getProjectPhaseIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(ProjectPhase.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<ProjectPhaseID>((Collection<? extends ProjectPhaseID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<ProjectPhase> getProjectPhases(Collection<ProjectPhaseID> projectPhaseIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectPhaseIDs, ProjectPhase.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueComment//
	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueComment> getIssueComments(Collection<IssueCommentID> issueCommentIDs, String[] fetchGroups,int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueCommentIDs, IssueComment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssueComment storeIssueComment(IssueComment issueComment, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueComment, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	//IssueLinkType//
	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueLinkType> getIssueLinkTypes(Collection<IssueLinkTypeID> issueLinkTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueLinkTypeIDs, IssueLinkType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs(Class<? extends Object> linkedObjectClass)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (linkedObjectClass == null) {
				linkedObjectClass = Object.class;
			}
			Set<IssueLinkType> issueLinkTypes = IssueLinkType.getIssueLinkTypes(pm, linkedObjectClass);
			return NLJDOHelper.getObjectIDSet(issueLinkTypes);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLinkType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueLinkTypeID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	//Issue//
	/**
	 * Stores the given Issue. If the issue is a new issue, do the initializing process instance.
	 * If not new, do the issue history creation process & check the assignee for doing the
	 * state assignment.
	 *
	 * @param issue The issue to be stored
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		Issue pIssue = null;
		try {
			//check if the issue is new.
			boolean isNewIssue = !JDOHelper.isDetached(issue);

			if (isNewIssue) {
				if (issue.getProject() == null) {
					issue.setProject((Project)pm.getObjectById(Project.PROJECT_ID_DEFAULT));
				}
				pIssue = pm.makePersistent(issue);

				IssueType type;

				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
					pm.flush();
					// create the ProcessInstance for new Issues
					// TODO: WORKAROUND: Calling createProcessInstanceForIssue on pIssue.getIssueType() says that this IssueType is not persistent ?!?
					type = (IssueType) pm.getObjectById(JDOHelper.getObjectId(pIssue.getIssueType()));
					if (type == null) {
						throw new IllegalStateException("Could not create ProcessInstance for new Issue as its type is null");
					}
				}
				else
					type = pIssue.getIssueType();

				type.createProcessInstanceForIssue(pIssue);
			}
			else {
				if (issue.getCreateTimestamp() != null) {
					issue.setUpdateTimestamp(new Date());
				}

				IssueID issueID = (IssueID) JDOHelper.getObjectId(issue);
				Issue oldPersistentIssue = (Issue) pm.getObjectById(issueID);

				User user = SecurityReflector.getUserDescriptor().getUser(pm);
				IssueHistory issueHistory = new IssueHistory(oldPersistentIssue.getOrganisationID(), user, oldPersistentIssue, issue, IDGenerator.nextID(IssueHistory.class));
				storeIssueHistory(issueHistory, false, new String[]{FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				if (issue.getCreateTimestamp() != null) {
					issue.setUpdateTimestamp(new Date());
				}

				boolean doUnassign;
				if (oldPersistentIssue == null)
					doUnassign = false;
				else {
					try {
						doUnassign = oldPersistentIssue.getAssignee() != null && issue.getAssignee() == null;
					} catch (JDODetachedFieldAccessException x) {
						doUnassign = false;
					}
				}

				boolean doAssign;
				try {
					doAssign = issue.getAssignee() != null;
					if (doAssign && oldPersistentIssue != null)
						doAssign = !Util.equals(issue.getAssignee(), oldPersistentIssue.getAssignee());
				} catch (JDODetachedFieldAccessException x) {
					doAssign = false;
				}

				String jbpmTransitionName = null;
				if (doAssign)
					jbpmTransitionName = JbpmConstants.TRANSITION_NAME_ASSIGN;

				if (doUnassign)
					jbpmTransitionName = JbpmConstants.TRANSITION_NAME_UNASSIGN;

				if (jbpmTransitionName != null) {
					// performing a transition might cause the fetch-plan to be modified => backup + restore
					FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
					try {
						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issue.getIssueLocal().getJbpmProcessInstanceId());
						if (processInstance.getRootToken().getNode().hasLeavingTransition(jbpmTransitionName))
							processInstance.signal(jbpmTransitionName);
					} finally {
						jbpmContext.close();
					}
					NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
				}

				pIssue = pm.makePersistent(issue);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(pIssue);
		} finally {
			pm.close();
		}
	}

	/**
	 * Delete an <code>Issue</code> by the given <code>IssueID</code>.
	 * And also delete its <code>State</code> and <code>IssueLocal</code>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void deleteIssue(IssueID issueID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(Issue.class, true);
			Issue issue = (Issue) pm.getObjectById(issueID);

			pm.getExtent(State.class, true);
			for (State state : issue.getStates()) {
				pm.deletePersistent(state);
			}
			pm.flush();

			pm.getExtent(IssueLocal.class, true);
			pm.deletePersistent(issue.getStatableLocal());
			pm.flush();

			pm.getExtent(Issue.class, true);
			pm.deletePersistent(issue);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * Signal the issue to change its <code>State</code>.
	 *
	 * @param issueID The issueID to be changed
	 * @param jbpmTransitionName a node name that defined in JbpmConstants
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			IssueLocal issueLocal = (IssueLocal) pm.getObjectById(IssueLocalID.create(issueID));
			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();

			try {
				ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issueLocal.getJbpmProcessInstanceId());
				processInstance.signal(jbpmTransitionName);
			} finally {
				jbpmContext.close();
			}

			pm.flush();

			if (get) {
				pm.getExtent(Issue.class);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
				else
					pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
				Issue issue = (Issue) pm.getObjectById(issueID);
				return pm.detachCopy(issue);
			}

		} finally {
			pm.close();
		}

		return null;
	}

	/**
	 * @param queries the QueryCollection containing all queries that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<IssueID> getIssueIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Issue.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<? extends AbstractSearchQuery> decoratedCollection;
			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<? extends AbstractSearchQuery>) queries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<AbstractSearchQuery>(queries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<? extends Issue> issues = (Collection<? extends Issue>) decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(issues);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueIDs, Issue.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueID> getIssueIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Issue.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueID>((Collection<? extends IssueID>) q.execute());
		} finally {
			pm.close();
		}
	}

	public Set<Issue> getIssueByProjectID(ProjectID projectID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getIssuesByProjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectID.organisationID);
			params.put("projectID", projectID.projectID);
			return NLJDOHelper.getObjectIDSet((Collection<Issue>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	public Set<Issue> getIssueByProjectTypeID(ProjectTypeID projectTypeID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getIssuesByProjectTypeID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectTypeID.organisationID);
			params.put("projectTypeID", projectTypeID.projectTypeID);
			return NLJDOHelper.getObjectIDSet((Collection<Issue>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	//IssueHistory//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssueHistory storeIssueHistory(IssueHistory issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueHistory, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<IssueHistoryID> getIssueHistoryIDsByIssueID(IssueID issueID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(IssueHistory.getIssueHistoryByIssue(pm, issueID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueHistory> getIssueHistories(Collection<IssueHistoryID> issueHistoryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueHistoryIDs, IssueHistory.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssueType storeIssueType(IssueType issueType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueType, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueType> getIssueTypes(Collection<IssueTypeID> issueTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueTypeIDs, IssueType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<IssueTypeID> getIssueTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			final Query allIDsQuery = pm.newNamedQuery(IssueType.class, IssueType.QUERY_ALL_ISSUETYPE_IDS);
			return new HashSet<IssueTypeID>((Collection<? extends IssueTypeID>)allIDsQuery.execute());
		} finally {
			pm.close();
		}
	}

	//IssuePriority//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issuePriority, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<IssuePriorityID> getIssuePriorityIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssuePriority.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssuePriorityID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssuePriority> getIssuePriorities(Collection<IssuePriorityID> issuePriorityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issuePriorityIDs, IssuePriority.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueSeverityType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try{
			return NLJDOHelper.storeJDO(pm, issueSeverityType, get, fetchGroups, maxFetchDepth);
		}//try
		finally{
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueSeverityTypeID> getIssueSeverityTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueSeverityType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueSeverityTypeID>((Collection<? extends IssueSeverityTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueSeverityType> getIssueSeverityTypes(Collection<IssueSeverityTypeID> issueSeverityTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueSeverityTypeIDs, IssueSeverityType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueResolution//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IssueResolution storeIssueResolution(IssueResolution issueResolution, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueResolution, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueResolutions(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueResolution.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueResolution> getIssueResolutions(Collection<IssueResolutionID> issueResolutionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueResolutionIDs, IssueResolution.class, fetchGroups, maxFetchDepth);
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
			// WORKAROUND JPOX Bug to avoid problems with creating workflows as State.statable is defined as interface and has subclassed implementations
			pm.getExtent(Issue.class);

			String organisationID = getOrganisationID();

			IssueStruct.getIssueStruct(organisationID, pm);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireIssueTrackingEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireIssueTrackingEAR.MODULE_NAME + " started...");

			pm.makePersistent(new ModuleMetaData(
					JFireIssueTrackingEAR.MODULE_NAME, "0.9.5.0.0.beta", "0.9.5.0.0.beta")
			);

			IssueType issueType = new IssueType(getOrganisationID(), "Default");
			issueType.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			issueType = pm.makePersistent(issueType);

			// Create the statuses
			IssueSeverityType issueSeverityType;

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Minor");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Major");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Crash");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Block");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Feature");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Trivial");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Text");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Tweak");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			IssuePriority issuePriority;

			issuePriority = new IssuePriority(getOrganisationID(), "None");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "None");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Low");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Low");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Normal");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Normal");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "High");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "High");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Urgent");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Urgent");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Immediate");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Immediate");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			// Create the resolutions
			IssueResolution issueResolution;

			issueResolution = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_ID_NOT_ASSIGNED.issueResolutionID);
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Not assigned");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Open");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Open");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Fixed");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Fixed");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Reopened");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Reopened");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "NotFixable");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Not fixable");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "WillNotFix");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Won't fix");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			// Create the process definitions.
			issueType.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			issueType = new IssueType(getOrganisationID(), "Customer");
			issueType.getName().setText(Locale.ENGLISH.getLanguage(), "Customer");
			issueType = pm.makePersistent(issueType);

			// Create the process definitions.
			issueType.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			// Create the issueLinkTypes
			IssueLinkType issueLinkType;

			issueLinkType = new IssueLinkType(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Related");
			issueLinkType.addLinkedObjectClass(Object.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Parent of");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Child of");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeDuplicate(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Duplicate");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			// Create the project type
			pm.getExtent(ProjectType.class);

			ProjectType projectType1 = new ProjectType(ProjectType.PROJECT_TYPE_ID_DEFAULT);
			projectType1.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			projectType1 = pm.makePersistent(projectType1);

			ProjectType projectType2 = new ProjectType(IDGenerator.getOrganisationID(), "cross ticket");
			projectType2.getName().setText(Locale.ENGLISH.getLanguage(), "Cross Ticket");
			projectType2 = pm.makePersistent(projectType2);

			ProjectType projectType3 = new ProjectType(IDGenerator.getOrganisationID(), "jfire");
			projectType3.getName().setText(Locale.ENGLISH.getLanguage(), "JFire");
			projectType3 = pm.makePersistent(projectType3);

			// Create the projects
			pm.getExtent(Project.class);

			Project project;

			project = new Project(Project.PROJECT_ID_DEFAULT);
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 1");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			//--
			Project subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 1");
			project.addSubProject(subProject);

			Project subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.1");
			subProject.addSubProject(subsubProject);

			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.2");
			subProject.addSubProject(subsubProject);

			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.3");
			subProject.addSubProject(subsubProject);

			//--
			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 2");
			project.addSubProject(subProject);

			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.1");
			subProject.addSubProject(subsubProject);

			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.2");
			subProject.addSubProject(subsubProject);

			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.3");
			subProject.addSubProject(subsubProject);
			//--
			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 3");
			project.addSubProject(subProject);

			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 4");
			project.addSubProject(subProject);

			//--
			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 2");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 3");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 4");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 5");
			project.setProjectType(projectType1);
			project = pm.makePersistent(project);

			// Create the project phases
			pm.getExtent(ProjectPhase.class);

			ProjectPhase projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase1");
			projectPhase.getName().setText(Locale.ENGLISH.getLanguage(), "Phase 1");
			projectPhase = pm.makePersistent(projectPhase);

			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase2");
			projectPhase.getName().setText(Locale.ENGLISH.getLanguage(), "Phase 2");
			projectPhase = pm.makePersistent(projectPhase);

			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase3");
			projectPhase.getName().setText(Locale.ENGLISH.getLanguage(), "Phase 3");
			projectPhase = pm.makePersistent(projectPhase);

			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase4");
			projectPhase.getName().setText(Locale.ENGLISH.getLanguage(), "Phase 4");
			projectPhase = pm.makePersistent(projectPhase);

			EditLockType issueEditLock = new EditLockType(EditLockTypeIssue.EDIT_LOCK_TYPE_ID);
			issueEditLock = pm.makePersistent(issueEditLock);
			//------------------------------------------------
		} finally {
			pm.close();
		}
	}
}
