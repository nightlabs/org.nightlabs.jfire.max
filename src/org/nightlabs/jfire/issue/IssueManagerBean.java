package org.nightlabs.jfire.issue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.jbpm.JbpmConstants;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.ProjectType;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.issue.prop.IssueStruct;
import org.nightlabs.jfire.issue.query.IssueQuery;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.Util;

/**
 * An EJB session bean provides methods for managing every objects used in the issue tracking.
 * <p>
 *
 * </p>
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class IssueManagerBean
extends BaseSessionBeanImpl
implements IssueManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	//IssueFileAttachment//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#getIssueWorkTimeRanges(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeProjectType(org.nightlabs.jfire.issue.project.ProjectType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeProject(org.nightlabs.jfire.issue.project.Project, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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

//	//ProjectPhase//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public ProjectPhase storeProjectPhase(ProjectPhase projectPhase, boolean get, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			//			boolean isNew = !JDOHelper.isDetached(projectPhase);
//			return NLJDOHelper.storeJDO(pm, projectPhase, get, fetchGroups, maxFetchDepth);
//		}//try
//		finally {
//			pm.close();
//		}//finally
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	@SuppressWarnings("unchecked")
//	public Set<ProjectPhaseID> getProjectPhaseIDs()
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Query q = pm.newQuery(ProjectPhase.class);
//			q.setResult("JDOHelper.getObjectId(this)");
//			return new HashSet<ProjectPhaseID>((Collection<? extends ProjectPhaseID>) q.execute());
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	@SuppressWarnings("unchecked")
//	public List<ProjectPhase> getProjectPhases(Collection<ProjectPhaseID> projectPhaseIDs, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			return NLJDOHelper.getDetachedObjectList(pm, projectPhaseIDs, ProjectPhase.class, fetchGroups, maxFetchDepth);
//		} finally {
//			pm.close();
//		}
//	}



	//IssueComment//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
@RolesAllowed("_Guest_")
	public List<IssueComment> getIssueComments(Collection<IssueCommentID> issueCommentIDs, String[] fetchGroups,int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueCommentIDs, IssueComment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssueComment(org.nightlabs.jfire.issue.IssueComment, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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

	//IssueLink//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public List<IssueLink> getIssueLinks(Collection<IssueLinkID> issueLinkIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueLinkIDs, IssueLink.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public Set<IssueLinkID> getIssueLinkIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLink.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueLinkID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public Collection<IssueLinkID> getIssueLinkIDsByOrganisationIDAndLinkedObjectID(String organisationID, String linkedObjectID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinksByOrganisationIDAndLinkedObjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", organisationID);
			params.put("linkedObjectID", linkedObjectID);

			return NLJDOHelper.getObjectIDSet((Collection<IssueLink>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	//Issue//
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssue(org.nightlabs.jfire.issue.Issue, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssueHistory(org.nightlabs.jfire.issue.history.IssueHistory, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssueType(org.nightlabs.jfire.issue.IssueType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssuePriority(org.nightlabs.jfire.issue.IssuePriority, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssueSeverityType(org.nightlabs.jfire.issue.IssueSeverityType, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
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
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#storeIssueResolution(org.nightlabs.jfire.issue.IssueResolution, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#getIssueResolutions(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public List<IssueResolution> getIssueResolutions(Collection<IssueResolutionID> issueResolutionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueResolutionIDs, IssueResolution.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#sendRemindEMail(java.lang.String, java.lang.String, org.nightlabs.jfire.security.id.UserID, java.util.Set)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void sendRemindEMail(String messageString, String subject, UserID senderID, Set<UserID> recipientIDs)
	{
		User sender = UserDAO.sharedInstance().getUser(senderID,
				new String[]{User.FETCH_GROUP_PERSON, User.FETCH_GROUP_NAME},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		String senderEmail = "";
		try {
			senderEmail = ((TextDataField)sender.getPerson().getDataField(PersonStruct.INTERNET_EMAIL)).getText();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<User> recipients = UserDAO.sharedInstance().getUsers(recipientIDs,
				new String[]{User.FETCH_GROUP_PERSON, User.FETCH_GROUP_NAME},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "classic.asianet.co.th");
		props.setProperty("mail.user", senderEmail);
		props.setProperty("mail.password", "");
		Session session = Session.getDefaultInstance(props, null);
		try {
			Transport transport = session.getTransport();
			MimeMessage message = new MimeMessage(session);
			message.setSubject(subject);
			message.setContent(messageString, "text/plain");
			message.setFrom(new InternetAddress(senderEmail));

			try {
				for (User recipient : recipients) {
					String recipientEmail =
						((TextDataField)recipient.getPerson().getDataField(PersonStruct.INTERNET_EMAIL)).getText();
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			message.setRecipient(Message.RecipientType.TO, new InternetAddress("chairat@guinaree.com"));

			transport.connect();
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		//		PersistenceManager pm = getPersistenceManager();
		//		try {
		//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		//			if (fetchGroups != null)
		//				pm.getFetchPlan().setGroups(fetchGroups);
		//
		//			Query q = pm.newQuery(IssueResolution.class);
		//		} finally {
		//			pm.close();
		//		}
	}

	//Bean//
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.IssueManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			UserID systemUserID = UserID.create(getOrganisationID(), getUserID());
			User systemUser = (User)pm.getObjectById(systemUserID);

			// WORKAROUND JPOX Bug to avoid problems with creating workflows as State.statable is defined as interface and has subclassed implementations
			pm.getExtent(Issue.class);

			IssueStruct.getIssueStruct(pm);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireIssueTrackingEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireIssueTrackingEAR.MODULE_NAME + " started...");

			pm.makePersistent(new ModuleMetaData(
					JFireIssueTrackingEAR.MODULE_NAME, "0.9.7-0-beta", "0.9.7-0-beta")
			);

			String baseName = "org.nightlabs.jfire.issue.resource.messages";
			ClassLoader loader = IssueManagerBean.class.getClassLoader();

			IssueType issueTypeDefault = new IssueType(getOrganisationID(), IssueType.DEFAULT_ISSUE_TYPE_ID);
			issueTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueTypeDefault"); //$NON-NLS-1$
			issueTypeDefault = pm.makePersistent(issueTypeDefault);

			// Create the statuses
			IssueSeverityType issueSeverityTypeMinor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityTypeMinor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeMinor"); //$NON-NLS-1$
			issueSeverityTypeMinor = pm.makePersistent(issueSeverityTypeMinor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMinor);

			IssueSeverityType issueSeverityTypeMajor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityTypeMajor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeMajor"); //$NON-NLS-1$
			issueSeverityTypeMajor = pm.makePersistent(issueSeverityTypeMajor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMajor);

			IssueSeverityType issueSeverityTypeCrash = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityTypeCrash.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeCrash"); //$NON-NLS-1$
			issueSeverityTypeCrash = pm.makePersistent(issueSeverityTypeCrash);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeCrash);

			IssueSeverityType issueSeverityTypeBlock = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityTypeBlock.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeBlock"); //$NON-NLS-1$
			issueSeverityTypeBlock = pm.makePersistent(issueSeverityTypeBlock);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeBlock);

			IssueSeverityType issueSeverityTypeFeature = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityTypeFeature.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeFeature"); //$NON-NLS-1$
			issueSeverityTypeFeature = pm.makePersistent(issueSeverityTypeFeature);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeFeature);

			IssueSeverityType issueSeverityTypeTrivial = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityTypeTrivial.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeTrivial"); //$NON-NLS-1$
			issueSeverityTypeTrivial = pm.makePersistent(issueSeverityTypeTrivial);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTrivial);

			IssueSeverityType issueSeverityTypeText = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityTypeText.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeText"); //$NON-NLS-1$
			issueSeverityTypeText = pm.makePersistent(issueSeverityTypeText);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeText);

			IssueSeverityType issueSeverityTypeTweak = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityTypeTweak.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeTweak"); //$NON-NLS-1$
			issueSeverityTypeTweak = pm.makePersistent(issueSeverityTypeTweak);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTweak);

			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			IssuePriority issuePriorityNone = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NONE);
			issuePriorityNone.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityNone"); //$NON-NLS-1$
			issuePriorityNone = pm.makePersistent(issuePriorityNone);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNone);

			IssuePriority issuePriorityLow = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_LOW);
			issuePriorityLow.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityLow"); //$NON-NLS-1$
			issuePriorityLow = pm.makePersistent(issuePriorityLow);
			issueTypeDefault.getIssuePriorities().add(issuePriorityLow);

			IssuePriority issuePriorityNormal = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NORMAL);
			issuePriorityNormal.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityNormal"); //$NON-NLS-1$
			issuePriorityNormal = pm.makePersistent(issuePriorityNormal);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNormal);

			IssuePriority issuePriorityHigh = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_HIGH);
			issuePriorityHigh.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityHigh"); //$NON-NLS-1$
			issuePriorityHigh = pm.makePersistent(issuePriorityHigh);
			issueTypeDefault.getIssuePriorities().add(issuePriorityHigh);

			IssuePriority issuePriorityUrgent = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_URGENT);
			issuePriorityUrgent.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityUrgent"); //$NON-NLS-1$
			issuePriorityUrgent = pm.makePersistent(issuePriorityUrgent);
			issueTypeDefault.getIssuePriorities().add(issuePriorityUrgent);

			IssuePriority issuePriorityImmediate = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_IMMEDIATE);
			issuePriorityImmediate.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityImmediate"); //$NON-NLS-1$
			issuePriorityImmediate = pm.makePersistent(issuePriorityImmediate);
			issueTypeDefault.getIssuePriorities().add(issuePriorityImmediate);

			// Create the resolutions
			IssueResolution issueResolutionNotAssigned = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_ID_NOT_ASSIGNED.issueResolutionID);
			issueResolutionNotAssigned.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionNotAssigned"); //$NON-NLS-1$
			issueResolutionNotAssigned = pm.makePersistent(issueResolutionNotAssigned);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotAssigned);

			IssueResolution issueResolutionOpen = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_OPEN);
			issueResolutionOpen.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionOpen"); //$NON-NLS-1$
			issueResolutionOpen = pm.makePersistent(issueResolutionOpen);
			issueTypeDefault.getIssueResolutions().add(issueResolutionOpen);

			IssueResolution issueResolutionFixed = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_FIXED);
			issueResolutionFixed.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionFixed"); //$NON-NLS-1$
			issueResolutionFixed = pm.makePersistent(issueResolutionFixed);
			issueTypeDefault.getIssueResolutions().add(issueResolutionFixed);

			IssueResolution issueResolutionReopened = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_REOPENED);
			issueResolutionReopened.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionReopened"); //$NON-NLS-1$
			issueResolutionReopened = pm.makePersistent(issueResolutionReopened);
			issueTypeDefault.getIssueResolutions().add(issueResolutionReopened);

			IssueResolution issueResolutionNotFixable = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_NOTFIXABLE);
			issueResolutionNotFixable.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionNotFixable"); //$NON-NLS-1$
			issueResolutionNotFixable = pm.makePersistent(issueResolutionNotFixable);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotFixable);

			IssueResolution issueResolutionWillNotFix = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_WILLNOTFIX);
			issueResolutionWillNotFix.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionWillNotFix"); //$NON-NLS-1$
			issueResolutionWillNotFix = pm.makePersistent(issueResolutionWillNotFix);
			issueTypeDefault.getIssueResolutions().add(issueResolutionWillNotFix);

			// Create the process definitions.
			issueTypeDefault.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			IssueType issueTypeCustomer = new IssueType(getOrganisationID(), "Customer");
			issueTypeCustomer.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueTypeCustomer"); //$NON-NLS-1$
			issueTypeCustomer = pm.makePersistent(issueTypeCustomer);

			// Create the process definitions.
			issueTypeCustomer.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			// Create the issueLinkTypes
			IssueLinkType issueLinkTypeRelated = new IssueLinkType(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);
			issueLinkTypeRelated.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeRelated"); //$NON-NLS-1$
			issueLinkTypeRelated.addLinkedObjectClass(Object.class);
			issueLinkTypeRelated = pm.makePersistent(issueLinkTypeRelated);

			IssueLinkType issueLinkTypeParent = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
			issueLinkTypeParent.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeParent"); //$NON-NLS-1$

			issueLinkTypeParent.addLinkedObjectClass(Issue.class);
			issueLinkTypeParent = pm.makePersistent(issueLinkTypeParent);

			IssueLinkType issueLinkTypeChild = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
			issueLinkTypeChild.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeChild"); //$NON-NLS-1$

			issueLinkTypeChild.addLinkedObjectClass(Issue.class);
			issueLinkTypeChild = pm.makePersistent(issueLinkTypeChild);

			IssueLinkType issueLinkTypeDuplicate = new IssueLinkTypeDuplicate(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE);
			issueLinkTypeDuplicate.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeDuplicate"); //$NON-NLS-1$

			issueLinkTypeDuplicate.addLinkedObjectClass(Issue.class);
			issueLinkTypeDuplicate = pm.makePersistent(issueLinkTypeDuplicate);

			// Create the project type
			pm.getExtent(ProjectType.class);

			ProjectType projectTypeDefault = new ProjectType(ProjectType.PROJECT_TYPE_ID_DEFAULT);
			projectTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.projectTypeDefault"); //$NON-NLS-1$

			projectTypeDefault = pm.makePersistent(projectTypeDefault);

			ProjectType projectTypeSoftware = new ProjectType(IDGenerator.getOrganisationID(), "software");
			projectTypeSoftware.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.software"); //$NON-NLS-1$

			projectTypeSoftware = pm.makePersistent(projectTypeSoftware);

			// Create the projects
			pm.getExtent(Project.class);

			Project projectDefault = new Project(Project.PROJECT_ID_DEFAULT);
			projectDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.projectDefault"); //$NON-NLS-1$

			projectDefault.setProjectType(projectTypeDefault);
			projectDefault = pm.makePersistent(projectDefault);

			Project jfireProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			jfireProject.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.jfireProject"); //$NON-NLS-1$
			jfireProject.setProjectType(projectTypeSoftware);
			jfireProject = pm.makePersistent(jfireProject);
//
//			//--
//			Project subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 1");
//			project.addSubProject(subProject);
//
//			Project subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.1");
//			subProject.addSubProject(subsubProject);
//
//			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.2");
//			subProject.addSubProject(subsubProject);
//
//			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 1.3");
//			subProject.addSubProject(subsubProject);
//
//			//--
//			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 2");
//			project.addSubProject(subProject);
//
//			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.1");
//			subProject.addSubProject(subsubProject);
//
//			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.2");
//			subProject.addSubProject(subsubProject);
//
//			subsubProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subsubProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub Sub project 2.3");
//			subProject.addSubProject(subsubProject);
//			//--
//			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 3");
//			project.addSubProject(subProject);
//
//			subProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			subProject.getName().setText(Locale.ENGLISH.getLanguage(), "Sub project 4");
//			project.addSubProject(subProject);

			//--
//			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 2");
//			project.setProjectType(projectType1);
//			project = pm.makePersistent(project);
//
//			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 3");
//			project.setProjectType(projectType1);
//			project = pm.makePersistent(project);
//
//			project = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			project.getName().setText(Locale.ENGLISH.getLanguage(), "Project 4");
//			project.setProjectType(projectType1);
//			project = pm.makePersistent(project);

//			projectDefault = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
//			projectDefault.getName().setText(Locale.ENGLISH.getLanguage(), "Project 5");
//			projectDefault.setProjectType(projectTypeDefault);
//			projectDefault = pm.makePersistent(projectDefault);

//			// Create the project phases
//			pm.getExtent(ProjectPhase.class);
//
//			ProjectPhase projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase1");
//			projectPhase.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.issue.IssueManagerBean.projectPhase1"); //$NON-NLS-1$
//			projectPhase = pm.makePersistent(projectPhase);
//
//			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase2");
//			projectPhase.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.issue.IssueManagerBean.projectPhase2"); //$NON-NLS-1$
//			projectPhase = pm.makePersistent(projectPhase);
//
//			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase3");
//			projectPhase.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.issue.IssueManagerBean.projectPhase3"); //$NON-NLS-1$
//			projectPhase = pm.makePersistent(projectPhase);
//
//			projectPhase = new ProjectPhase(IDGenerator.getOrganisationID(), "phase4");
//			projectPhase.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.issue.IssueManagerBean.projectPhase4"); //$NON-NLS-1$
//			projectPhase = pm.makePersistent(projectPhase);

			//Issues
			pm.getExtent(Issue.class);

			Issue issue1 = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class), issueTypeDefault);
			issue1.setIssuePriority(issuePriorityHigh);
			issue1.setIssueResolution(issueResolutionOpen);
			issue1.setIssueSeverityType(issueSeverityTypeMinor);
			issue1.setReporter(systemUser);
			IssueSubject subject1 = new IssueSubject(issue1);
			subject1.readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.subject1"); //$NON-NLS-1$
			issue1.setSubject(subject1);

			storeIssue(issue1, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			Issue issue2 = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class), issueTypeDefault);
			issue2.setIssuePriority(issuePriorityHigh);
			issue2.setIssueResolution(issueResolutionOpen);
			issue2.setIssueSeverityType(issueSeverityTypeMinor);
			issue2.setReporter(systemUser);
			IssueSubject subject2 = new IssueSubject(issue2);
			subject2.readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.subject2"); //$NON-NLS-1$
			issue2.setSubject(subject2);

			storeIssue(issue2, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			Issue issue3 = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class), issueTypeDefault);
			issue3.setIssuePriority(issuePriorityHigh);
			issue3.setIssueResolution(issueResolutionOpen);
			issue3.setIssueSeverityType(issueSeverityTypeMinor);
			issue3.setReporter(systemUser);
			IssueSubject subject3 = new IssueSubject(issue3);
			subject3.readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.subject3"); //$NON-NLS-1$
			issue3.setSubject(subject3);

			storeIssue(issue3, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			Issue issue4 = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class), issueTypeDefault);
			issue4.setIssuePriority(issuePriorityHigh);
			issue4.setIssueResolution(issueResolutionOpen);
			issue4.setIssueSeverityType(issueSeverityTypeMinor);
			issue4.setReporter(systemUser);
			IssueSubject subject4 = new IssueSubject(issue4);
			subject4.readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.subject4"); //$NON-NLS-1$
			issue4.setSubject(subject4);

			storeIssue(issue4, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			Issue issue5 = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class), issueTypeDefault);
			issue5.setIssuePriority(issuePriorityHigh);
			issue5.setIssueResolution(issueResolutionOpen);
			issue5.setIssueSeverityType(issueSeverityTypeMinor);
			issue5.setReporter(systemUser);
			IssueSubject subject5 = new IssueSubject(issue5);
			subject5.readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.subject5"); //$NON-NLS-1$
			issue5.setSubject(subject5);

			storeIssue(issue5, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			//Predefined Query Stores
			IssueQuery unassignedIssueIssueQuery = new IssueQuery();
			unassignedIssueIssueQuery.setAssigneeID(null);

			IssueQuery resolvedIssueIssueQuery = new IssueQuery();
			resolvedIssueIssueQuery.setStateDefinitionName(JbpmConstants.NODE_NAME_RESOLVED);

			IssueQuery modifiedIssueIssueQuery = new IssueQuery();

			//1 Unassigned Issues
			pm.getExtent(BaseQueryStore.class);

			QueryCollection<IssueQuery> queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(unassignedIssueIssueQuery);

			BaseQueryStore queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
	//		queryStore.getName().setText(Locale.ENGLISH.getLanguage(), "Unassigned");
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreUnassigned"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//2 Resolved Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(resolvedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
//			queryStore.getName().setText(Locale.ENGLISH.getLanguage(), "Resolved");
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreResolved"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			EditLockType issueEditLock = new EditLockType(EditLockTypeIssue.EDIT_LOCK_TYPE_ID);
			issueEditLock = pm.makePersistent(issueEditLock);
			//------------------------------------------------
		} finally {
			pm.close();
		}
	}
}